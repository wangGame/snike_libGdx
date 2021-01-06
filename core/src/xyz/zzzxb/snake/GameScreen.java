package xyz.zzzxb.snake;


import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class GameScreen implements Screen {
    private Stage stage;
    @Override
    public void show() {
        stage = new Stage(SnakeMain.viewport,SnakeMain.batch);
        GameView view = new GameView();
        stage.addActor(view);
    }

    @Override
    public void render(float delta) {
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
