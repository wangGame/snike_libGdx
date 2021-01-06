package xyz.zzzxb.snake;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

import org.omg.PortableInterceptor.DISCARDING;

public class GameView extends Group {
    private Node head;
    private NodeSnike snike;
    private Action tempAction;
    private Direction currentDir;
    public GameView(){
        setSize(Constant.width,Constant.hight);
        setDebug(true);
        snike = new NodeSnike(100,100);
        head = snike.getHead();
        tempAction =Actions.forever(Actions.delay(0.4F,Actions.run(
                ()->{
                    snike.addNode();
                    if (snike.getNum() >=10){
                        removeAction(tempAction);
                    }
            }
        )));
        addAction(tempAction);
        currentDir = Direction.LEFT;

        InputAdapter adapter = new InputAdapter(){
            @Override
            public boolean keyDown(int keycode) {
                System.out.println(keycode);
                return super.keyDown(keycode);
            }
        };
        Gdx.input.setInputProcessor(adapter);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        time += delta;
        if (time >1){
            time = 0;
            move();
        }
        handler();
    }

    private float time;
    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        head = snike.getHead();
        while (head!= null) {
            head.draw(batch);
            head = head.next;
        }
    }
    private Vector2 current = new Vector2(0,0);
    private Vector2 prePos = new Vector2(Integer.MAX_VALUE,Integer.MAX_VALUE);
    public void move(){
        head = snike.getHead();
        while (head !=null){
            if (prePos.x == Integer.MAX_VALUE) {
                prePos.set(head.getX(),head.getY());
                if (currentDir == Direction.UP){
                    current.set(head.getX(),head.getY()+10);
                }else if (currentDir == Direction.DOWN){
                    current.set(head.getX(),head.getY()-10);
                }else if (currentDir == Direction.LEFT){
                    current.set(head.getX()-10,head.getY());
                }else if (currentDir == Direction.RIGHT){
                    current.set(head.getX()+10,head.getY());
                }
                head.setX((int)current.x);
                head.setY((int)current.y);
            }else {
                float tempx = prePos.x;
                float tempy = prePos.y;
                prePos.set(head.getX(),head.getY());
                head.setX((int)tempx);
                head.setY((int)tempy);
            }
            head = head.next;
        }
        prePos.set(Integer.MAX_VALUE,Integer.MAX_VALUE);
    }


    public void handler(){
        //上  下   左  右
        if (Gdx.input.isKeyPressed(19)) {
            currentDir = Direction.UP;
        }else if (Gdx.input.isKeyPressed(20)){
            currentDir = Direction.DOWN;
        }else if (Gdx.input.isKeyPressed(21)){
            currentDir = Direction.LEFT;
        }else if (Gdx.input.isKeyPressed(22)){
            currentDir = Direction.RIGHT;
        }
    }
}
