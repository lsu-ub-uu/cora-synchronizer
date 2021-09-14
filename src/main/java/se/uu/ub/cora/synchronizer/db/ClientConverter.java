package se.uu.ub.cora.synchronizer.db;

import se.uu.ub.cora.clientdata.ClientDataElement;

public interface ClientConverter {

	ClientDataElement convert(String dataString);

}
