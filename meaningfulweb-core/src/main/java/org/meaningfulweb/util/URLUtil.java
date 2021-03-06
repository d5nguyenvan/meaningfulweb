package org.meaningfulweb.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.meaningfulweb.util.domain.DomainSuffix;
import org.meaningfulweb.util.domain.DomainSuffixes;

/** Utility class for URL analysis */
public class URLUtil {
	
	 private final static String           TOP_LEVEL_DOMAINS =
		    "((?i)aero|asia|biz|cat|com|coop|edu|gov|info|int|jobs|mil|mobi|museum|name|net|org|pro|tel|travel|" +
		    "[a-z]{2})";
	  private final static String           URL_HTTP = "(ht|f)tp(s?)\\:\\/\\/";
	  
	  
	  private final static String           URL_REGEX_STRING_POST = 
	      // example               (group #)
	     "([\\w]+:\\w+@)?" +                             // username:password@    (4)
	     "(" +
	     "((www\\.)?" +                                // www.                  (7)
	     "(([a-zA-Z0-9][\\w\\-]*\\.)+" +             // ddrfreak.             (8)
	     TOP_LEVEL_DOMAINS + "))" +                  // com                   (8)(9)
	     "|" +
	     "((\\d{1,3})(\\.\\d{1,3}){3})" +              // 64.71.156.35
	     ")" +                                           
	     "(:[\\d]{1,5})?" +                              // :80
	     "(" +  // optional                              //                       (15)

	     "(" +  // any directory or filename block, if present, must start with "/"
	     "(/+)" +                                    // /
	     "([\\S&&[^</\"]]+/+)*" +                    // locations/
	     "([\\S&&[^</\"]]*" +                            
	     "([\\S&&[^</\".,;:!>'\\])\\?]])" +        // locations
	     "(\\.[\\w]{3,4})?" +                      // .php
	     ")?" +
	     ")?" +

	     "(" +
	     "(\\?\\w+(=[%\\w]+)?)" +                    // ?action=displayLocation
	     "(&\\w+(=\\w+)?)*" +                        // &locationID=265
	     ")?" +
	     ")?";
	  
	  private final static Pattern          URL_INCOMPLETE_REGEX = 
		    Pattern.compile("(" + URL_HTTP + ")?" +             // http:// (optional)
		                    URL_REGEX_STRING_POST);
	  
	  public final static int DOMAIN_NAME = 8;
	  
	  public static String extractDomainFromUrl(String url)
	  {
	    if (url==null || url.length() == 0) return null;
	    Matcher matcher = URL_INCOMPLETE_REGEX.matcher(url);
	    if(matcher == null) return null;
	    if (matcher.matches())
	    {
	      String group = matcher.group(DOMAIN_NAME);
	      if (group == null)
	        return null;
	      return group.trim().toLowerCase();
	    }
	      return null;
	    }

  private static Pattern IP_PATTERN = Pattern
    .compile("(\\d{1,3}\\.){3}(\\d{1,3})");

  /** Returns the domain name of the url. The domain name of a url is
   *  the substring of the url's hostname, w/o subdomain names. As an
   *  example <br><code>
   *  getDomainName(conf, new URL(http://lucene.apache.org/))
   *  </code><br>
   *  will return <br><code> apache.org</code>
   *   */
  public static String getDomainName(URL url) {
    DomainSuffixes tlds = DomainSuffixes.getInstance();
    String host = url.getHost();
    // it seems that java returns hostnames ending with .
    if (host.endsWith("."))
      host = host.substring(0, host.length() - 1);
    if (IP_PATTERN.matcher(host).matches())
      return host;

    int index = 0;
    String candidate = host;
    for (; index >= 0;) {
      index = candidate.indexOf('.');
      String subCandidate = candidate.substring(index + 1);
      if (tlds.isDomainSuffix(subCandidate)) {
        return candidate;
      }
      candidate = subCandidate;
    }
    return candidate;
  }

  /** Returns the domain name of the url. The domain name of a url is
   *  the substring of the url's hostname, w/o subdomain names. As an
   *  example <br><code>
   *  getDomainName(conf, new http://lucene.apache.org/)
   *  </code><br>
   *  will return <br><code> apache.org</code>
   * @throws MalformedURLException
   */
  public static String getDomainName(String url)
    throws MalformedURLException {
    return getDomainName(new URL(url));
  }

  /** Returns whether the given urls have the same domain name.
   * As an example, <br>
   * <code> isSameDomain(new URL("http://lucene.apache.org")
   * , new URL("http://people.apache.org/"))
   * <br> will return true. </code>
   *
   * @return true if the domain names are equal
   */
  public static boolean isSameDomainName(URL url1, URL url2) {
    return getDomainName(url1).equalsIgnoreCase(getDomainName(url2));
  }

  /**Returns whether the given urls have the same domain name.
  * As an example, <br>
  * <code> isSameDomain("http://lucene.apache.org"
  * ,"http://people.apache.org/")
  * <br> will return true. </code>
  * @return true if the domain names are equal
  * @throws MalformedURLException
  */
  public static boolean isSameDomainName(String url1, String url2)
    throws MalformedURLException {
    return isSameDomainName(new URL(url1), new URL(url2));
  }

  /** Returns the {@link DomainSuffix} corresponding to the
   * last public part of the hostname
   */
  public static DomainSuffix getDomainSuffix(URL url) {
    DomainSuffixes tlds = DomainSuffixes.getInstance();
    String host = url.getHost();
    if (IP_PATTERN.matcher(host).matches())
      return null;

    int index = 0;
    String candidate = host;
    for (; index >= 0;) {
      index = candidate.indexOf('.');
      String subCandidate = candidate.substring(index + 1);
      DomainSuffix d = tlds.get(subCandidate);
      if (d != null) {
        return d;
      }
      candidate = subCandidate;
    }
    return null;
  }

  /** Returns the {@link DomainSuffix} corresponding to the
   * last public part of the hostname
   */
  public static DomainSuffix getDomainSuffix(String url)
    throws MalformedURLException {
    return getDomainSuffix(new URL(url));
  }

  /** Partitions of the hostname of the url by "."  */
  public static String[] getHostSegments(URL url) {
    String host = url.getHost();
    // return whole hostname, if it is an ipv4
    // TODO : handle ipv6
    if (IP_PATTERN.matcher(host).matches())
      return new String[]{host};
    return host.split("\\.");
  }

  /** Partitions of the hostname of the url by "."
   * @throws MalformedURLException */
  public static String[] getHostSegments(String url)
    throws MalformedURLException {
    return getHostSegments(new URL(url));
  }

  /**
   * Returns the lowercased hostname for the url or null if the url is not well
   * formed.
   * 
   * @param url The url to check.
   * @return String The hostname for the url.
   */
  public static String getHost(String url) {
    try {
      return new URL(url).getHost().toLowerCase();
    }
    catch (MalformedURLException e) {
      return null;
    }
  }

  /**
   * Returns the page for the url.  The page consists of the protocol, host,
   * and path, but does not include the query string.  The host is lowercased
   * but the path is not.
   * 
   * @param url The url to check.
   * @return String The page for the url.
   */
  public static String getPage(String url) {
    try {
      // get the full url, and replace the query string with and empty string
      url = url.toLowerCase();
      String queryStr = new URL(url).getQuery();
      return (queryStr != null) ? url.replace("?" + queryStr, "") : url;
    }
    catch (MalformedURLException e) {
      return null;
    }
  }

  public static String getProtocol(String url) {
    try {
      // get the full url, and replace the query string with and empty string
      url = url.toLowerCase();
      String protocolStr = new URL(url).getProtocol();
      return protocolStr;
    }
    catch (MalformedURLException e) {
      return null;
    }
  }

  public static String stripProtocol(String url) {
    try {
      // get the full url, and replace the query string with and empty string
      url = url.toLowerCase();
      String protocolStr = new URL(url).getProtocol();
      return (protocolStr != null) ? url.replace(protocolStr + "://", "") : url;
    }
    catch (MalformedURLException e) {
      return null;
    }
  }

  public static String getExtension(String filename) {
    int extIndex = StringUtils.lastIndexOf(filename, ".");
    return extIndex > 0 && extIndex + 1 < filename.length() ? StringUtils
      .substring(filename, extIndex + 1) : null;
  }

  public static String toAbsoluteURL(String baseURL, String relativeURL) {
    
    // first we try to use java.net.URL to perform the conversion, if that
    // fails we can try using our own routine.
    try {
      URL base = new URL(baseURL);
      String absolute = new URL(base, relativeURL).toExternalForm();
      return absolute;
    }
    catch (MalformedURLException e) {

      if (baseURL == null || baseURL.length() < 8)
        throw new IllegalArgumentException("baseURL must be a valid URL");
      if (relativeURL == null)
        return null;

      // rooted relative URL
      if (relativeURL.startsWith("/")) {
        int pos = baseURL.indexOf("/", 8);
        if (pos > -1) {
          baseURL = baseURL.substring(0, pos);
        }
      }
      else {
        int slashPosition = baseURL.lastIndexOf('/');
        if (slashPosition < 0)
          throw new IllegalArgumentException("baseURL must be a valid URL");
        baseURL = baseURL.substring(0, slashPosition);
        relativeURL = "/" + relativeURL;
      }

      return baseURL + relativeURL;
    }
  }
}
