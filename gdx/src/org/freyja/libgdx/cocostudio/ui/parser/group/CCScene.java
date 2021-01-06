package org.freyja.libgdx.cocostudio.ui.parser.group;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.Json;
import org.freyja.libgdx.cocostudio.ui.CocoStudioUIEditor;
import org.freyja.libgdx.cocostudio.ui.model.CCExport;
import org.freyja.libgdx.cocostudio.ui.model.ObjectData;
import org.freyja.libgdx.cocostudio.ui.parser.GroupParser;
import org.freyja.libgdx.cocostudio.ui.util.JsonUtil;

/**
 * @tip 还未支持单色背景属性,背景图片在Cocostudio里面并不是铺满,而是居中
 * @author i see
 * 
 */
public class CCScene extends GroupParser {

	private static ArrayMap<String,CCExport> exports = new ArrayMap<>();
	@Override
	public String getClassName() {
//		return "LayerObjectData";
		return "ProjectNodeObjectData";
	}

	@Override
	public Actor parse(CocoStudioUIEditor editor, ObjectData widget) {
		String name = editor.getDirName()+widget.getFileData().getPath();
		CCExport export = exports.get(name);
		if(export == null){
			FileHandle handle = Gdx.files.internal(name);
			String json = handle.readString("utf-8");
			Json jj = JsonUtil.getJson();
			jj.setIgnoreUnknownFields(true);
			export = jj.fromJson(CCExport.class, json);
			exports.put(name,export);
		}
		Group group = editor.createGroup(export);
		group.setTouchable(Touchable.enabled);

		return group;
	}

	public static Group parse(CocoStudioUIEditor editor,String name){
		CCExport export = exports.get(name);
		if(export == null) {
			FileHandle handle = Gdx.files.internal(name);
			String json = handle.readString("utf-8");
			Json jj = JsonUtil.getJson();
			jj.setIgnoreUnknownFields(true);
			export = jj.fromJson(CCExport.class, json);
			exports.put(name,export);
		}
		return editor.createGroup(export);

	}

}
