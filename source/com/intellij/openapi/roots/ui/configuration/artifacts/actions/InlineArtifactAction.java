package com.intellij.openapi.roots.ui.configuration.artifacts.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.ProjectBundle;
import com.intellij.openapi.roots.ui.configuration.artifacts.*;
import com.intellij.openapi.roots.ui.configuration.artifacts.nodes.PackagingElementNode;
import com.intellij.packaging.artifacts.Artifact;
import com.intellij.packaging.elements.CompositePackagingElement;
import com.intellij.packaging.elements.PackagingElement;
import com.intellij.packaging.impl.elements.ArtifactPackagingElement;

/**
 * @author nik
 */
public class InlineArtifactAction extends AnAction {
  private final ArtifactsEditor myEditor;

  public InlineArtifactAction(ArtifactsEditor editor) {
    super(ProjectBundle.message("action.name.inline.artifact"));
    myEditor = editor;
  }

  @Override
  public void update(AnActionEvent e) {
    final LayoutTreeSelection selection = myEditor.getPackagingElementsTree().getSelection();
    final PackagingElementNode<?> node = selection.getNodeIfSingle();
    PackagingElement<?> element = selection.getElementIfSingle();
    e.getPresentation().setEnabled(element instanceof ArtifactPackagingElement && node != null && node.getParentElement(element) != null);
  }

  public void actionPerformed(AnActionEvent e) {
    final LayoutTreeComponent treeComponent = myEditor.getPackagingElementsTree();
    final LayoutTreeSelection selection = treeComponent.getSelection();
    final PackagingElement<?> element = selection.getElementIfSingle();
    final PackagingElementNode<?> node = selection.getNodeIfSingle();
    if (node == null || !(element instanceof ArtifactPackagingElement)) return;

    CompositePackagingElement<?> parent = node.getParentElement(element);
    if (parent == null) {
      return;
    }
    if (!treeComponent.checkCanRemove(selection.getNodes())) return;
    if (!treeComponent.checkCanAdd(null, parent, node)) return;

    treeComponent.ensureRootIsWritable();
    parent.removeChild(element);
    final Artifact artifact = ((ArtifactPackagingElement)element).findArtifact(myEditor.getContext());
    if (artifact != null) {
      for (PackagingElement<?> child : artifact.getRootElement().getChildren()) {
        parent.addChild(ArtifactUtil.copyWithChildren(child));
      }
    }
    treeComponent.rebuildTree();
  }
}
