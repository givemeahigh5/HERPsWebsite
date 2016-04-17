package edu.elon.herps.servlet;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.*;

import org.apache.commons.io.IOUtils;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;


//@SuppressWarnings("serial")
public class CreateFileUploadServlet extends HttpServlet {

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		//Reads 1 arg from formdata: "target", which is the url target for success after blob upload
		//creates an upload url string and returns it to the app
		//(the url uploads any files included in the multipart/formdata to blobstore, then redirects to 'target')
		//the app then posts to the generated url to complete the upload

		ServletFileUpload upload = new ServletFileUpload();

		try {
			if(ServletFileUpload.isMultipartContent(req)) {

				FileItemIterator items = upload.getItemIterator(req);
				resp.setContentType("text/plain");

				while(items.hasNext()) {
					FileItemStream item = items.next();
					InputStream stream = item.openStream();

					if(item.getFieldName().equals("target")) {
						String target = IOUtils.toString(stream, "UTF-8");

						BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
						String url = blobstoreService.createUploadUrl(target);
						resp.getWriter().write(url);
					}
					else {
						resp.getWriter().write("fail");
					}
				}
			}
			else {
				resp.setContentType("text/plain");
				resp.getWriter().write("fail");
			}
		} catch (Exception e) {
			throw new IOException(e);
		}
	}


	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		/*try {
			String target = req.getParameter("target");
			resp.setContentType("text/plain");

			if(target != null) {
				BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
				String url = blobstoreService.createUploadUrl(target);
				resp.getWriter().write(url);
			}
			else {
				resp.getWriter().write("fail");
			}
		} catch (Exception e) {
			throw new IOException(e);
		}*/
	}
}
