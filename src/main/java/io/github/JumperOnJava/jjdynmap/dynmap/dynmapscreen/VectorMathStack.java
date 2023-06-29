package io.github.JumperOnJava.jjdynmap.dynmap.dynmapscreen;

import net.minecraft.util.math.Vec3d;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class VectorMathStack {
    private List<TwoSideOperation<Vec3d>> operations = new LinkedList<>();
    public Deque<List<TwoSideOperation<Vec3d>>> stack = new LinkedList<>();
    public VectorMathStack() {
    }
    public void push(){
        stack.push(new LinkedList<>(operations));
    }
    public void pop(){
        operations=stack.pop();
    }
    public List<TwoSideOperation<Vec3d>> getCurrentOperations(){
        return new LinkedList<>(operations);
    }
    public void translate(double x,double y){
        var t = new TranslateOperation(x,y);
        operations.add(t);
    }

    public void scale(double s){
        var t = new MultiplyOperation(s);
        operations.add(t);
    }
    public static Vec3d applyForward(Vec3d base, List<TwoSideOperation<Vec3d>> operations)
    {
        var vec = base;
        for(var operation : operations)
        {
            vec = operation.getForwardFunction().apply(vec);
        }
        return vec;
    }
    public static Vec3d undo(Vec3d target, List<TwoSideOperation<Vec3d>> operations)
    {
        var iterator = operations.listIterator(operations.size());
        while (iterator.hasPrevious()){
            target = iterator.previous().getBackwardFunction().apply(target);
        }
        return target;
    }

}
