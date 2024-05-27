package tage;

import java.util.UUID;

import tage.*;
import tage.shapes.*;
import org.joml.*;
import java.lang.Math;

public class AsteroidOre extends GameObject{
    private int index;
    private float initialScale;

    public AsteroidOre(GameObject p, ObjShape s, int index){
        super(p, s);
        this.index = index;
        this.initialScale = this.getLocalScale().get(0,0);
    }

    public AsteroidOre(GameObject p, ObjShape s, TextureImage t, int index){
        super(p, s, t);
        this.index = index;
        this.initialScale = this.getLocalScale().get(0,0);

    }

    public int getIndex(){
        return this.index;
    }

    public float getInitialScale(){
        return this.initialScale;
    }

    public void setInitialScale(float scale){
        this.initialScale = scale;
    }

}