package io.github.javajump3r.jjdynmap.dynmap.dynmapscreen;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.javajump3r.jjdynmap.dynmap.DynMapHelper;
import io.github.javajump3r.jjdynmap.dynmap.DynMapRenderer;
import io.github.javajump3r.jjdynmap.dynmap.TextureRequest;
import io.github.javajump3r.jjdynmap.dynmap.waypoints.Waypoint;
import io.github.javajump3r.jjdynmap.dynmap.waypoints.WaypointStorage;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class DynmapWorldScreen extends Screen {

    Vec2f mapCenter;
    float pixelsPerBlock=1;
    int currentZoomLevel=0;
    int distance = 6;
    float sizeTarget=.3f;
    float size=.3f;
    public DynmapWorldScreen(Text title) {
        super(title);
    }
    public void init(){
        mapCenter = new Vec2f((float) client.player.getX(),(float) client.player.getZ()+32);

    }
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float delta){
        renderBackground(matrixStack);
        var mathStack = new VectorMathStack();

        matrixStack.push();
        mathStack.push();

        matrixStack.translate(width/2,height/2,0);
        mathStack.translate(width/2,height/2);

        matrixStack.push();
        mathStack.push();

        int n=4;
        size = (size*n+sizeTarget)/(n+1);

        currentZoomLevel=5;
        //if(size>.1) currentZoomLevel=4;
        if(size>.2) currentZoomLevel=3;
        //if(size>.4) currentZoomLevel=2;
        if(size>.8) currentZoomLevel=1;
        //if(size>1.6) currentZoomLevel=0;

        matrixStack.scale(size,size,1);
        mathStack.scale(1/size);

        matrixStack.translate(-mapCenter.x, -mapCenter.y, 0);
        mathStack.translate(-mapCenter.x, -mapCenter.y);
        int[] sizes={5,currentZoomLevel};
        var worldSpaceOperations = mathStack.getCurrentOperations();

        for(int s=0;s<sizes.length;s++)
        {
            var blocksPerLevel = DynMapHelper.getBlocksPerZoomLevel(sizes[s]);
            for (int i = -distance-5; i <= distance+4; i++) {
                for (int j = -distance; j <= distance; j++) {
                    var player = client.player;
                    var request = TextureRequest.WorldSpaceTextureRequest(
                            DynMapRenderer.mapLink,
                            DynMapHelper.getCurrentWorld(),
                            sizes[s],
                            (int) mapCenter.x,
                            (int) mapCenter.y,
                            i,
                            j);
                    if(request.getTexture()==TextureRequest.ERROR_TEXTURE)
                        continue;
                    RenderSystem.setShaderTexture(0, request.getTexture());
                    drawTexture(matrixStack,
                            (request.worldX),
                            (request.worldY),
                            0, 0,
                            blocksPerLevel, blocksPerLevel,
                            blocksPerLevel, blocksPerLevel);
                }
            }
        }


        /*fill(matrixStack,1,5000,0,-5000, ColorHelper.Argb.getArgb(255,255,0,0));
        fill(matrixStack,5000,1,-5000,0, ColorHelper.Argb.getArgb(255,0,0,255));*/
        matrixStack.pop();
        mathStack.pop();
        int r = 4;
        int col;
        col = ColorHelper.Argb.getArgb(255,128,128,128);
        fill(matrixStack,r,r,-r,r-1,col);
        fill(matrixStack,r,r,r-1,-r,col);
        col = ColorHelper.Argb.getArgb(255,196,196,196);
        fill(matrixStack,-r,-r,r,-r+1,col);
        fill(matrixStack,-r,-r,-r+1,r,col);


        matrixStack.pop();
        mathStack.pop();
        var clientWaypoint = WaypointStorage.getMainInstance().getClientWaypoint();
        var currentWorld = DynMapHelper.getCurrentWorld();
        for(var waypoint : WaypointStorage.getMainInstance().getAllWaypoints()){

            if(!Waypoint.isInSameGroup(waypoint.getDimension(),currentWorld))
                continue;
            var waypointPos2d = waypoint.getPosInDimension(currentWorld);
            var waypointPos= new Vec3d(-waypointPos2d.x, -waypointPos2d.y-32, 0);
            waypointPos = VectorMathStack.undo(waypointPos,worldSpaceOperations);
            waypoint.RenderWaypointOnScreen(matrixStack,-waypointPos.x,-waypointPos.y);
        }
        var pos = VectorMathStack.applyForward(new Vec3d(-width/2,-height/2,0),worldSpaceOperations).negate();
        //var coordsText = DynMapHelper.vecToString(new Vec2f((float)pos.x,(float)pos.y));

        var textX = "X: "+ (int) pos.x;
        var textZ = "Z: "+ (int) pos.y;
        var widthX = textRenderer.getWidth(textX);
        var widthZ = textRenderer.getWidth(textZ);
        fill(matrixStack,0,0,widthX+2,10,ColorHelper.Argb.getArgb(128,0,0,0));
        fill(matrixStack,0,10,widthZ+2,20,ColorHelper.Argb.getArgb(128,0,0,0));
        client.textRenderer.draw(matrixStack, textX,1,2,ColorHelper.Argb.getArgb(255,255,255,255));
        client.textRenderer.draw(matrixStack, textZ,1,12,ColorHelper.Argb.getArgb(255,255,255,255));
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if(button != 0)
            return true;
        //LavaJumper.log("MapCenter: ",mapCenter.x,mapCenter.y);
        mapCenter = mapCenter.add(new Vec2f((float) -deltaX*1/size,(float) -deltaY*1/size));
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        sizeTarget= (float) (sizeTarget + amount * sizeTarget/4);
        sizeTarget=MathHelper.clamp(sizeTarget,0.05f,15);
        return super.mouseScrolled(mouseX, mouseY, amount);
    }
}
