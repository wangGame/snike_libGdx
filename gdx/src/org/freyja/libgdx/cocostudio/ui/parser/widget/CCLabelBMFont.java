package org.freyja.libgdx.cocostudio.ui.parser.widget;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;

import com.badlogic.gdx.utils.Align;
import org.freyja.libgdx.cocostudio.ui.CocoStudioUIEditor;
import org.freyja.libgdx.cocostudio.ui.model.CColor;
import org.freyja.libgdx.cocostudio.ui.model.ObjectData;
import org.freyja.libgdx.cocostudio.ui.parser.WidgetParser;

public class CCLabelBMFont extends WidgetParser {
    public String TAG = "CCLabelBMFont";

    @Override
    public String getClassName() {
        return "TextBMFontObjectData";
    }

    @Override
    public Actor parse(CocoStudioUIEditor editor, ObjectData widget) {
        BitmapFont font = null;
        if (editor.getBitmapFonts() != null) {
            font = editor.getBitmapFonts().get(widget.getLabelBMFontFile_CNB().getPath());
        }
        if (font == null) {// 备用创建字体方式
            font = new BitmapFont(Gdx.files.internal(editor.getDirName() + widget.getLabelBMFontFile_CNB().getPath()));
        }

        if (font == null) {
            editor.debug(widget, "BitmapFont字体:" + widget.getLabelBMFontFile_CNB().getPath() + " 不存在");
            font = new BitmapFont();
        }

        font.setUseIntegerPositions(false);
        font.getData().ascent = 0;
        font.getData().capHeight = font.getLineHeight();
        LabelStyle style = new LabelStyle(font, Color.WHITE);
        Label label = new Label(widget.getLabelText(), style);

        if (widget.getAnchorPoint().getScaleX() == 1) {
            if (widget.getAnchorPoint().getScaleY() == 0) {
                label.setAlignment(Align.bottomRight,Align.right);
            } else if (widget.getAnchorPoint().getScaleY() == 1) {
                label.setAlignment(Align.topRight,Align.right);
            } else {
                label.setAlignment( Align.center,Align.right);
            }

        } else if (widget.getAnchorPoint().getScaleX() == 0) {
            if (widget.getAnchorPoint().getScaleY() == 0) {
                label.setAlignment(Align.bottomLeft);
            } else if (widget.getAnchorPoint().getScaleY() == 1) {
                label.setAlignment(Align.topLeft,Align.left);
            } else {
                label.setAlignment(Align.left,Align.left);
            }
        } else {
            if (widget.getAnchorPoint().getScaleY() == 0) {
                label.setAlignment(Align.bottom,Align.center);
            } else if (widget.getAnchorPoint().getScaleY() == 1) {
                label.setAlignment(Align.top,Align.center);
            } else {
                label.setAlignment(Align.center,Align.center);
            }
        }
//        label.setFontScale(widget.getScale().getScaleX(),
//                widget.getScale().getScaleY());
        return label;
    }

}
