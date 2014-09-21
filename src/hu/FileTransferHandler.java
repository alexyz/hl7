package hu;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.List;

import javax.swing.TransferHandler;

/**
 * don't add this to text areas because it breaks cut and paste
 */
public class FileTransferHandler extends TransferHandler {
	@Override
	public boolean canImport (TransferSupport support) {
		return true;
	}
	
	@Override
	public boolean importData (TransferSupport support) {
		System.out.println("import");
	    Transferable t = support.getTransferable();
		try {
			List<?> l = (List<?>) t.getTransferData(DataFlavor.javaFileListFlavor);
			System.out.println("files=" + l);
			for (Object o : l) {
				if (o instanceof File) {
					EditorJFrame.getInstance().addFileEditor((File) o);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("could not drop", e);
		}
		return true;
	}
}