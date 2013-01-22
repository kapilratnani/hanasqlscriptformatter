package net.ripper.hana.sqlscriptformatter.handlers;

import net.ripper.hana.sqlscriptformatter.SQLScriptFormatter;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class CommandHandler extends AbstractHandler {
	/**
	 * The constructor.
	 */
	public CommandHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {

		IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event)
				.getActivePage();
		if (page != null) {
			IEditorPart part = page.getActiveEditor();

			ITextEditor editor = (ITextEditor) part;
			IDocumentProvider dp = editor.getDocumentProvider();
			IDocument doc = dp.getDocument(editor.getEditorInput());

			ISelection selection = editor.getSelectionProvider().getSelection();
			ITextSelection textSelection = null;
			String content = "";
			if (selection != null) {
				textSelection = (ITextSelection) selection;
				if (textSelection.getLength() > 0) {
					content = textSelection.getText();
				} else {
					content = doc.get();
				}
			}

			if (textSelection != null && textSelection.getLength() > 0) {
				try {
					int lineOffset = doc.getLineOffset(textSelection
							.getStartLine());

					String formattedSql = new SQLScriptFormatter()
							.format(content);
					doc.replace(lineOffset, textSelection.getLength(),
							formattedSql);
				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// else {
			// try {
			// String formattedSql = new SQLFormatter(content, 0, 1)
			// .format();
			// doc.replace(0, doc.getLength(), formattedSql);
			// } catch (BadLocationException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			// }
		}

		return null;
	}
}
