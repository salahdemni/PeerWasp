package org.peerbox.forcesync;

import java.nio.file.Path;

import org.hive2hive.core.processes.files.list.FileNode;
import org.peerbox.watchservice.PathUtils;
import org.peerbox.watchservice.filetree.composite.FileComponent;

public class FileInfo {
	private Path path;
	private boolean isFolder;
	private String contentHash;

	/**
	 * Creates a new FileInfo instance with hash set to null.
	 *
	 * @param path
	 * @param isFolder
	 */
	public FileInfo(Path path, boolean isFolder) {
		this.path = path;
		this.contentHash = "";
		this.isFolder = isFolder;
	}
	
	/**
	 * Creates a new FileInfo instance with hash set to null.
	 *
	 * @param path
	 * @param isFolder
	 */
	public FileInfo(Path path, boolean isFolder, String hash) {
		this.path = path;
		this.contentHash = hash;
		this.isFolder = isFolder;
	}


	/**
	 * Creates a new FileInfo instance given a FileComponent.
	 * All attributes are copied from the component.
	 *
	 * @param component
	 */
	public FileInfo(FileComponent component) {
		this.path = component.getPath();
		this.contentHash = component.getContentHash();
		this.isFolder = component.isFolder();
	}

	/**
	 * Creates a new FileInfo instance given a FileNode.
	 * All attributes are copied from the node.
	 *
	 * @param node
	 */
	public FileInfo(FileNode node) {
		this.path = node.getFile().toPath();
		this.isFolder = node.isFolder();
		if (node.isFile()) {
			// FileNode hash is null for folders!
			contentHash = (node.getMd5() != null) ? PathUtils.base64Encode(node.getMd5()) : "";
		}
	}

	public Path getPath() {
		return path;
	}

	public String getContentHash() {
		return contentHash;
	}

	public FileInfo setContentHash(String contentHash) {
		this.contentHash = contentHash;
		return this;
	}

	public boolean isFile() {
		return !isFolder();
	}

	public boolean isFolder() {
		return isFolder;
	}
}