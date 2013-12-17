simple-oauthserver
==================

Simple Oauth2 Server example on top of Reaseasy and Apache Oltu.

This code was based on Apache Oltu documentation and integration tests.

##To run the oauth server:

	mvn clean package
	java -jar target/dependency/jetty-runner.jar target/oauthserver.war

##See:

[OAuth2 Spec](http://tools.ietf.org/html/rfc6749)

[OAuth2 website](http://oauth.net/2/)

[Apache Oltu](http://oltu.apache.org/)

[Resteasy](http://www.jboss.org/resteasy/docs)


