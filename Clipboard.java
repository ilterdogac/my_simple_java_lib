import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
//import java.awt.event.WindowEvent;
import java.io.IOException;


public class Clipboard {
	// DONE: Move those to another source file because they aren't nearly as core as
	//       the other fn.java methods. Maybe to a new called Clipboard.java???
	
	private static final java.awt.datatransfer.Clipboard cb = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
	private static java.awt.datatransfer.Clipboard clipboard() {return cb;}
	
	public static String readString() {
		try {return asString(readContent());}
		catch (UnsupportedFlavorException e) {throw new IllegalStateException("There is no plain text in the clipboard", e);}
		catch (IOException e) {throw new RuntimeException(e);}
	}
	public static void writeString(String text) throws IOException {
		try {writeContent(new java.awt.datatransfer.StringSelection(text));}
		catch (UnsupportedFlavorException e) {throw new Error(e);}
	}
	
	public static String readFormattedText() {
		try {return asHTML(readContent());}
		catch (UnsupportedFlavorException e) {throw new IllegalStateException("There is no formatted text in the clipboard", e);}
		catch (IOException e) {throw new RuntimeException(e);}
	}
	public static void writeFormattedText(String formattedTextAsHTML) throws IOException {
		try {writeContent(fromHTML(formattedTextAsHTML));}
		catch (UnsupportedFlavorException e) {throw new Error(e);}
	}
	
	
	private static String asString(java.awt.datatransfer.Transferable awtTransferable)
	throws IOException, UnsupportedFlavorException {
		return (String) awtTransferable.getTransferData(
			java.awt.datatransfer.DataFlavor.stringFlavor
		);
	}
	private static String asHTML(java.awt.datatransfer.Transferable awtTransferable)
	throws IOException, UnsupportedFlavorException {
		return asHTML_allFlavor(awtTransferable);
	}
	
	
	// FIXME: Implement your own method to obtain the image!!
	// TODO: Replace buffered concrete class with the abstract super class
	// Warning: java.awt.datatransfer.DataFlavor.imageFlavor is null when java.awt.Image or java.desktop
	// has a problem or absence
	public static java.awt.image.BufferedImage asAwtImage(java.awt.datatransfer.Transferable awtTransferable)
	throws IOException, UnsupportedFlavorException {
		return (java.awt.image.BufferedImage) awtTransferable.getTransferData(
			java.awt.datatransfer.DataFlavor.imageFlavor
		);
	}
	
	// FIXME: Implement your own method to transfer the image it to PNG format!!
	// FIXME: Depends on java.awt.Graphics
	// FIXME: Depends on javax.imageio.write
	public static byte[] asPNGImage(java.awt.image.BufferedImage awtImage)
	throws IOException, UnsupportedFlavorException {
		java.awt.Graphics g = awtImage.getGraphics();
		ByteIO.OutputStream os = new ByteIO.OutputStream();
		javax.imageio.ImageIO.write(awtImage, "png", os);
		return os.dump();
	}
	
	
	private static java.awt.datatransfer.Transferable fromImage(byte[] pngImage) {
		return new ImageTransferable(pngImage);
	}
	
	private static java.awt.datatransfer.Transferable fromPNGImage(byte[] pngImage)
	throws IOException, UnsupportedFlavorException {
		return fromImage(pngImage); // Supports PNG I guess (what else would it if not PNG??)
	}
	
	
	private static String asHTML_allFlavor(java.awt.datatransfer.Transferable awtTransferable)
	throws IOException, UnsupportedFlavorException {
		java.awt.datatransfer.DataFlavor flavor = java.awt.datatransfer.DataFlavor.allHtmlFlavor;
//			allHtmlFlavor, selectionHtmlFlavor, fragmentHtmlFlavor
		return (String) awtTransferable.getTransferData(flavor);
	}
	
	
	private static abstract interface ImplTransferable extends java.awt.datatransfer.Transferable {
		public default boolean isDataFlavorSupported(java.awt.datatransfer.DataFlavor flavor) {
			for (java.awt.datatransfer.DataFlavor expectedFlavor: getTransferDataFlavors())
				if (flavor.equals(expectedFlavor)) return true;
			return false;
		}
	}
	
	private static class ImageTransferable implements ImplTransferable {
		// Maybe static??
		private final java.awt.datatransfer.DataFlavor
			imageFlavor = java.awt.datatransfer.DataFlavor.imageFlavor;
		
		private final byte[] imageContent;
		
		public ImageTransferable(byte[] imageContent) {
			this.imageContent = fn.clone(imageContent);
		}
		
		public java.awt.datatransfer.DataFlavor[] getTransferDataFlavors() {
			return new java.awt.datatransfer.DataFlavor[] {imageFlavor};
		}
		
		public Object getTransferData(java.awt.datatransfer.DataFlavor flavor)
		throws UnsupportedFlavorException, IOException {
			if (!isDataFlavorSupported(flavor)) throw new UnsupportedFlavorException(flavor);
			else return fromImage(imageContent);
		}
		
	}
	
	private static class HTMLTransferable implements ImplTransferable {
		// Maybe static??
		private final java.awt.datatransfer.DataFlavor
			plainFlavor = java.awt.datatransfer.DataFlavor.stringFlavor,
			htmlFlavor = java.awt.datatransfer.DataFlavor.allHtmlFlavor;
		
		// HTMLText is the xml-looking html representation of the formatted string.
		private final String htmlText, plainVersion;
		public HTMLTransferable(String htmlText, String plainVersion) {
			this.htmlText = htmlText;
			this.plainVersion = plainVersion;
		}
		public HTMLTransferable(String htmlText) {this (htmlText, null);}
		
		public java.awt.datatransfer.DataFlavor[] getTransferDataFlavors() {
			return new java.awt.datatransfer.DataFlavor[] {plainFlavor, htmlFlavor};
		}
		
		public Object getTransferData(java.awt.datatransfer.DataFlavor flavor)
		throws UnsupportedFlavorException, IOException {
			if (flavor == htmlFlavor) return htmlText;
			if (flavor == plainFlavor) {
				String pl = plainVersion;
				if (pl == null) pl = "(formatted text \u2014 paste as formatted text)";
				return pl;
			}
			throw new UnsupportedFlavorException(flavor);
		}
	}
	
	private static java.awt.datatransfer.Transferable fromHTML(String htmlText) {
		return new HTMLTransferable(htmlText);
	}
	
	public static java.awt.datatransfer.Transferable readContent() {
		return clipboard().getContents(null);
	}
	private static void writeContent(java.awt.datatransfer.Transferable awtTransferable)
	throws IOException, UnsupportedFlavorException {
		clipboard().setContents(awtTransferable, null);
	}
}