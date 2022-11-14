package xyz.zzzxb.snake;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

public class Node {
    private int x = 0;
    private int y = 0;
    public xyz.zzzxb.snake.Node next;
    private int num;
    private Image image;
    public Node(int num ,int x, int y) {
        this.x = x;
        this.y = y;
        this.num = num;
        image = new Image(new Texture("snike.png"));
        image.setPosition(x,y);
    }

    public void draw(Batch batch){
        image.draw(batch,1);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
        postionChange();
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
        postionChange();
    }

    public void postionChange(){
        image.setPosition(x,y);
    }
}