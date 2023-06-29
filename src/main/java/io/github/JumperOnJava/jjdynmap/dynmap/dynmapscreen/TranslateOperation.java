package io.github.JumperOnJava.jjdynmap.dynmap.dynmapscreen;

import net.minecraft.util.math.Vec3d;

import java.util.function.Function;

public class TranslateOperation implements TwoSideOperation<Vec3d> {
    private final double x;
    private final double y;
    public TranslateOperation(double x, double y) {
        this.x=x;
        this.y=y;
    }

    @Override
    public Function<Vec3d, Vec3d> getForwardFunction() {
        return translateFunction(x,y);
    }

    @Override
    public Function<Vec3d, Vec3d> getBackwardFunction() {
        return translateFunction(-x,-y);
    }
    private Function<Vec3d,Vec3d> translateFunction(double x,double y){
        var function =new Function<Vec3d,Vec3d>(){
            double x,y;
            @Override
            public Vec3d apply(Vec3d in) {
                return in.add(new Vec3d(x,y,0));
            }
        };
        function.x=x;
        function.y=y;
        return function;
    }
}
