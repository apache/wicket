package org.apache.wicket.devutils.debugbar;

import java.io.Serializable;

import org.apache.wicket.Component;

public interface IDebugBarContributor extends Serializable {

	Component createComponent(String id, WicketDebugBar debugBar);

}
