package hu;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.util.List;

final class DT extends DropTarget {
	@Override
	public synchronized void drop (DropTargetDropEvent dtde) {
		System.out.println("drop " + dtde);
		dtde.acceptDrop(DnDConstants.ACTION_REFERENCE);
	    Transferable t = dtde.getTransferable();
		try {
			List<?> l = (List<?>) t.getTransferData(DataFlavor.javaFileListFlavor);
			System.out.println("files=" + l);
			for (Object o : l) {
				if (o instanceof File) {
					EditorJFrame.frame.addEditor((File) o);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("could not drop", e);
		}
	}
	
	@Override
	public synchronized void dragOver (DropTargetDragEvent dtde) {
		dtde.acceptDrag(DnDConstants.ACTION_LINK);
	}
}
