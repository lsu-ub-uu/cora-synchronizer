module se.uu.ub.cora.synchronizer {
	requires se.uu.ub.cora.clientdata;
	requires se.uu.ub.cora.javaclient;
	requires transitive se.uu.ub.cora.xmlutils;
	requires se.uu.ub.cora.converter;
	requires se.uu.ub.cora.data;

	requires se.uu.ub.cora.logger;
	requires java.activation;
	requires java.ws.rs;
	requires jakarta.servlet;
	requires java.xml.bind;
}