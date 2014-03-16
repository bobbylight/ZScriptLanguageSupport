/*
 * 07/29/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.ImageIcon;



/**
 * Holds icons used by ZScript auto-completion.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class IconFactory {

	public static final String SOURCE_FILE_ICON			= "sourceFileIcon";
	public static final String IMPORT_ROOT_ICON			= "importRootIcon";
	public static final String IMPORT_ICON				= "importIcon";
	public static final String METHOD_PUBLIC_ICON		= "methodPublicIcon";
	public static final String FIELD_PUBLIC_ICON		= "fieldPublicIcon";
	public static final String SCRIPT_ICON				= "scriptIcon";
	public static final String TEMPLATE_ICON			= "templateIcon";

	public static final String CONSTANT_ICON			= "constructorIcon";

	public static final String ERROR_ICON				= "errorIcon";
	public static final String WARNING_ICON				= "warningIcon";

	private Map<String, Icon> iconMap;

	private static final IconFactory INSTANCE = new IconFactory();


	private IconFactory() {

		iconMap = new HashMap<String, Icon>();
		iconMap.put(SOURCE_FILE_ICON, loadIcon("jcu_obj.gif"));
		iconMap.put(IMPORT_ROOT_ICON, loadIcon("impc_obj.gif"));
		iconMap.put(IMPORT_ICON, loadIcon("imp_obj.gif"));
		iconMap.put(METHOD_PUBLIC_ICON, loadIcon("methpub_obj.gif"));
		iconMap.put(FIELD_PUBLIC_ICON, loadIcon("field_public_obj.gif"));
		iconMap.put(SCRIPT_ICON, loadIcon("script.png"));
		iconMap.put(TEMPLATE_ICON, loadIcon("template_obj.gif"));

		iconMap.put(CONSTANT_ICON, loadIcon("constr_ovr.gif"));

		iconMap.put(ERROR_ICON, loadIcon("error_obj.gif"));
		iconMap.put(WARNING_ICON, loadIcon("warning_obj.gif"));

	}


	public static IconFactory get() {
		return INSTANCE;
	}


	public Icon getIcon(String key) {
		return iconMap.get(key);
	}


	public Icon getIcon(IconData data) {
		// TODO: Optimize me
		DecoratableIcon icon = new DecoratableIcon(16, getIcon(data.getIcon()));
		if (data.isConstant()) {
			icon.addDecorationIcon(getIcon(CONSTANT_ICON));
		}
		return icon;
	}


	public String getIconUrl(IconData data) {
		Icon baseIcon = getIcon(data.getIcon());
		return ((ImageIcon)baseIcon).getDescription();
	}


	private Icon loadIcon(String name) {
		URL res = getClass().getResource("img/" + name);
		if (res==null) {
			// IllegalArgumentException is what would be thrown if res
			// was null anyway, we're just giving the actual arg name to
			// make the message more descriptive
			throw new IllegalArgumentException("icon not found: img/" + name);
		}
		return new ImageIcon(res);
	}


	public static class IconData {

		private String icon;
		private boolean constant;

		public IconData(String icon, boolean constant) {
			this.icon = icon;
			this.constant = constant;
		}

		/**
		 * Returns the main icon to use when rendering this member's completion.
		 * This icon will be decorated appropriately based on whether it is
		 * abstract, deprecated, final, static, or any of the above.
		 *
		 * @return The icon to use.
		 */
		public String getIcon() {
			return icon;
		}

		public boolean isConstant() {
			return constant;
		}

	}


}