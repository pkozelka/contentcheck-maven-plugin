package net.sf.buildbox.maven.contentcheck;

import org.springframework.util.AntPathMatcher;

/**
 * Path matcher that supports Ant like patterns.
 * 
 * <p>The mapping matches URLs using the following rules:<br> <ul> <li>? matches one character</li> <li>* matches zero
 * or more characters</li> <li>** matches zero or more 'directories' in a path</li> </ul>
 *
 * <p>Some examples:<br> <ul> <li><code>com/t?st.jsp</code> - matches <code>com/test.jsp</code> but also
 * <code>com/tast.jsp</code> or <code>com/txst.jsp</code></li> <li><code>com/*.jsp</code> - matches all
 * <code>.jsp</code> files in the <code>com</code> directory</li> <li><code>com/&#42;&#42;/test.jsp</code> - matches all
 * <code>test.jsp</code> files underneath the <code>com</code> path</li> <li><code>org/springframework/&#42;&#42;/*.jsp</code>
 * - matches all <code>.jsp</code> files underneath the <code>org/springframework</code> path</li>
 * <li><code>org/&#42;&#42;/servlet/bla.jsp</code> - matches <code>org/springframework/servlet/bla.jsp</code> but also
 * <code>org/springframework/testing/servlet/bla.jsp</code> and <code>org/servlet/bla.jsp</code></li> </ul>
 * 
 */
public class PathMatcher {

	private static final AntPathMatcher matcherImpl = new AntPathMatcher();
	
	public boolean match(String pattern, String path) {
		//FIXME dagi: backed by AntPathMatcher from Spring for now. We should find some
		//common util class for it or write own implementation and do not depend on Spring 
		//because of one class...
		return matcherImpl.match(pattern, path);
	}
}
