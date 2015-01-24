package org.peerbox.app.config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.peerbox.utils.NetUtils;
import org.peerbox.utils.OsUtils;
import org.peerbox.utils.WinRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;

/**
 * Configuration of several application properties such as user names and path variables.
 * Changes are written to disk (config file) as soon as a value is set or updated.
 *
 * @Singleton: At the moment, the user configuration is maintained as a singleton because of the
 * fixed file names (and writing to the file needs to be synchronized)
 *
 * @author albrecht
 *
 */
@Singleton
public class UserConfig implements IUserConfig{

	private static final Logger logger = LoggerFactory.getLogger(UserConfig.class);

	/**
	 * The default configuration (note: resource, not a file)
	 */
	private static final String DEFAULT_PROPERTIES_FILENAME = "/config/default_user";

	/**
	 * The property names
	 */
	private static final String PROPERTY_AUTO_LOGIN = "autologin";
	private static final String PROPERTY_USERNAME = "username";
	private static final String PROPERTY_PASSWORD = "password";
	private static final String PROPERTY_PIN = "pin";
	private static final String PROPERTY_ROOTPATH = "rootpath";
	private static final String PROPERTY_API_SERVER_PORT = "api_server_port";

	/**
	 * Separator character for the serialization of lists of values
	 */
	private static final String LIST_SEPARATOR = ",";

	private final Path propertyFile;
	private Properties prop;

	/**
	 * Creates a new user configuration using the given file name.
	 *
	 * @param filename the filename of the property file
	 */
	public UserConfig(final Path file) {
		this.propertyFile = file;
	}

	/**
	 * Loads the default and user properties from disk
	 *
	 * @throws IOException if loading of file fails
	 */
	public synchronized void load() throws IOException {
		if (propertyFile == null) {
			throw new IllegalStateException(
					"No filename for the user config file given (propertyFile = null)");
		}

		// first read defaults
		Properties defaultProp = loadDefaultProperties();

		// create parent dirs and empty file if not exists yet
		if (!Files.exists(propertyFile)) {
			if (!Files.exists(propertyFile.getParent())) {
				Files.createDirectories(propertyFile.getParent());
			}
			Files.createFile(propertyFile);
		}
		prop = loadUserProperties(defaultProp);
		logger.info("Loaded property file: {}", propertyFile.toAbsolutePath());
	}

	/**
	 * Stores the current properties on disk
	 *
	 * @throws IOException
	 */
	private synchronized void saveProperties() throws IOException {
		try (OutputStream out = new FileOutputStream(propertyFile.toFile())) {
			prop.store(out, null);
		}
	}

	/**
	 * Loads the default properties
	 *
	 * @return default properties instance
	 * @throws IOException
	 */
	private Properties loadDefaultProperties() throws IOException {
		try (InputStream in = getClass().getResourceAsStream(DEFAULT_PROPERTIES_FILENAME)) {
			Properties defaultProps = new Properties();
			defaultProps.load(in);
			return defaultProps;
		}
	}

	/**
	 * Loads the user properties. The default and user properties are merged
	 *
	 * @param defaultProp default properties for merging config
	 * @return user properties instance
	 * @throws IOException
	 */
	private Properties loadUserProperties(final Properties defaultProp) throws IOException {
		try (InputStream in = new FileInputStream(propertyFile.toFile())) {
			Properties p = new Properties(defaultProp);
			p.load(in);
			return p;
		}
	}

	/**
	 * @return root path
	 */
	public synchronized Path getRootPath() {
		String p = prop.getProperty(PROPERTY_ROOTPATH);
		return hasRootPath() ? Paths.get(p) : null;
	}

	/**
	 * @return true if there is a root path set, false otherwise.
	 */
	public synchronized boolean hasRootPath() {
		return prop.getProperty(PROPERTY_ROOTPATH) != null && !prop.getProperty("rootpath").isEmpty();
	}

	/**
	 * Sets the root path.
	 *
	 * @param path if null or empty, the root path is removed from the config.
	 * @throws IOException
	 */
	public synchronized void setRootPath(final Path path) throws IOException {
		if (path != null && !path.toString().isEmpty()) {

			prop.setProperty(PROPERTY_ROOTPATH, path.toString());

			if(OsUtils.isWindows()) {
				WinRegistry.setRootPath(path);
			}

		} else {
			prop.remove(PROPERTY_ROOTPATH);
		}
		saveProperties();
	}

	/**
	 * @return the username
	 */
	public synchronized String getUsername() {
		String n = prop.getProperty(PROPERTY_USERNAME);
		return n != null ? n.trim() : n;
	}

	/**
	 * @return true if there is a username set, false otherwise.
	 */
	public synchronized boolean hasUsername() {
		return getUsername() != null && !getUsername().isEmpty();
	}

	/**
	 * Sets the username. Username is trimmed.
	 *
	 * @param username if null or empty, the username is removed from the config.
	 * @throws IOException
	 */
	public synchronized void setUsername(final String username) throws IOException {
		if(username != null && !username.isEmpty()) {
			prop.setProperty(PROPERTY_USERNAME, username.trim());
		} else {
			prop.remove(PROPERTY_USERNAME);
		}
		saveProperties();
	}

	/**
	 * @return the password
	 */
	public synchronized String getPassword() {
		return prop.getProperty(PROPERTY_PASSWORD);
	}

	/**
	 * @return true if there is a password set, false otherwise.
	 */
	public synchronized boolean hasPassword() {
		return getPassword() != null && !getPassword().isEmpty();
	}

	/**
	 * Sets the password.
	 *
	 * @param password if null or empty, the password is removed from the config.
	 * @throws IOException
	 */
	public synchronized void setPassword(final String password) throws IOException {
		if (password != null && !password.isEmpty()) {
			prop.setProperty(PROPERTY_PASSWORD, password);
		} else {
			prop.remove(PROPERTY_PASSWORD);
		}
		saveProperties();
	}

	/**
	 * @return the pin
	 */
	public synchronized String getPin() {
		return prop.getProperty(PROPERTY_PIN);
	}

	/**
	 * @return true if there is a pin set, false otherwise.
	 */
	public synchronized boolean hasPin() {
		return getPin() != null && !getPin().isEmpty();
	}

	/**
	 * Sets the pin.
	 *
	 * @param pin if null or empty, the password is removed from the config.
	 * @throws IOException
	 */
	public synchronized void setPin(final String pin) throws IOException {
		if(pin != null && !pin.isEmpty()) {
			prop.setProperty(PROPERTY_PIN, pin);
		} else {
			prop.remove(PROPERTY_PIN);
		}
		saveProperties();
	}

	/**
	 * @return true if auto login is set, false otherwise.
	 */
	public synchronized boolean isAutoLoginEnabled() {
		return Boolean.valueOf(prop.getProperty(PROPERTY_AUTO_LOGIN));
	}

	/**
	 * Sets the auto login property.
	 *
	 * @param enabled
	 * @throws IOException
	 */
	public synchronized void setAutoLogin(boolean enabled) throws IOException {
		prop.setProperty(PROPERTY_AUTO_LOGIN, Boolean.toString(enabled));
		saveProperties();
	}

	/**
	 * @return the port of the rest api server
	 */
	public synchronized int getApiServerPort() {
		String p = prop.getProperty(PROPERTY_API_SERVER_PORT);
		return p != null ? Integer.valueOf(p.trim()) : -1;
	}

	/**
	 * @return true if there is a server port is set, false otherwise.
	 */
	public synchronized boolean hasApiServerPort() {
		int port = getApiServerPort();
		return NetUtils.isValidPort(port);
	}

	/**
	 * Sets the api server port.
	 *
	 * @param port if not in valid range, the port is removed from the config.
	 * @throws IOException
	 */
	public synchronized void setApiServerPort(final int port) throws IOException {
		if(NetUtils.isValidPort(port)) {

			prop.setProperty(PROPERTY_API_SERVER_PORT, String.valueOf(port));

			if(OsUtils.isWindows()) {
				WinRegistry.setApiServerPort(port);
			}

		} else {
			prop.remove(PROPERTY_API_SERVER_PORT);
		}
		saveProperties();
	}

	/**
	 * @return the path to the config file
	 */
	public Path getConfigFileName() {
		return propertyFile;
	}

}