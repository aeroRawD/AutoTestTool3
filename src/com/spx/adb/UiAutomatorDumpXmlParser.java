package com.spx.adb;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

public class UiAutomatorDumpXmlParser {
	private List<UiNode> uiNodeList = new ArrayList<UiNode>();

	public List<UiNode> getUiNodeList() {
		return uiNodeList;
	}

	public UiAutomatorDumpXmlParser() {

	}

	public List<UiNode> parse(Document document) {
		treeWalk(document.getRootElement());
		return getUiNodeList();
	}

	public void treeWalk(Element element) {
		for (int i = 0, size = element.nodeCount(); i < size; i++) {
			Node node = element.node(i);

			if (node instanceof Element) {
				Element subElement = (Element) node;
				UiNode uiNode = new UiNode(subElement);
				uiNodeList.add(uiNode);
				treeWalk(subElement);
			} else {
				// do something....
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
