package io.github.javajump3r.jjdynmap.dynmap.dynmapscreen;

import net.minecraft.util.math.Vec3d;

import java.util.function.Function;

public class MultiplyOperation implements TwoSideOperation<Vec3d> {
    private final double s;
    public MultiplyOperation(double s) {
        this.s=s;
    }

    @Override
    public Function<Vec3d, Vec3d> getForwardFunction() {
        return multiplyFunction(s);
    }

    @Override
    public Function<Vec3d, Vec3d> getBackwardFunction() {
        return multiplyFunction(1/s);
    }
    private Function<Vec3d,Vec3d> multiplyFunction (double multiplier){
        var function = new Function<Vec3d,Vec3d>(){
            double s;
            @Override
            public Vec3d apply(Vec3d in) {
                return new Vec3d(in.x*s, in.y*s, in.z*s);
            }
        };
        function.s=multiplier;
        return function;
    }
}
