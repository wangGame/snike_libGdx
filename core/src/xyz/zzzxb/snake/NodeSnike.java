package xyz.zzzxb.snake;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

public class NodeSnike {
    private Node head;
    private int num = 0;
    public NodeSnike(int startX,int startY){
        addNum();
        head = new Node(num,startX,startY);
        head.next = null;
    }

    public void addNum(){
        num++;
    }

    public void addNode (){
        addNum();
        Node node = new Node(num,head.getX()+10,head.getY());
        Node temp = head;
        node.next = temp;
        head = node;
    }

    public int getNum() {
        return num;
    }

    public Node getHead() {
        return head;
    }

//    public static void main(String[] args) {
//        NodeSnike snike = new NodeSnike(1, 1);
//        for (int i = 0; i < 10; i++) {
//            snike.addNode();
//        }
//    }
}