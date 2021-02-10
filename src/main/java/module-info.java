module se.uu.ub.cora.synchronizer {
	requires transitive se.uu.ub.cora.clientdata;
	requires transitive se.uu.ub.cora.javaclient;
	requires transitive se.uu.ub.cora.httphandler;

	requires se.uu.ub.cora.logger;
	requires java.activation;
	requires java.ws.rs;
	requires javax.servlet.api;
	requires java.xml.bind;
	requires encoder;
}