package org.eclipse.epsilon.zest.eol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.epsilon.eol.IEolExecutableModule;
import org.eclipse.epsilon.eol.dom.Operation;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.execute.context.IEolContext;
import org.eclipse.epsilon.zest.EpsilonZestPlugin;

/**
 * <p>
 * Utility class that runs parts of an EOL module dedicated to Epsilon Zest to
 * provide various useful services on top of it.
 * </p>
 * 
 * <p>
 * This class should not depend on any Zest-specific logic, and it should not
 * handle any threading issues on its own - clients should be careful to only
 * invoke these methods from the UI thread.
 * </p>
 */
public class EpsilonZestModuleWrapper {

	public static final String OP_NODE_LABEL = "nodeLabel";
	public static final String OP_OUTGOING = "outgoing";
	public static final String OP_PROPERTIES = "properties";
	public static final String OP_SETPROPERTY = "setProperty";

	private final IEolExecutableModule module;

	public EpsilonZestModuleWrapper(IEolExecutableModule module) {
		this.module = module;
	}

	public IEolExecutableModule getModule() {
		return module;
	}

	public Iterable<Object> getInitialNodes() {
		for (Operation op : module.getOperations()) {
			if (op.hasAnnotation("initial")) {
				try {
					Object result = op.execute(null, Collections.emptyList(), module.getContext());
					return adaptToIterable(result);
				} catch (EolRuntimeException e) {
					EpsilonZestPlugin.getDefault().logException(e);
				}
			}
		}
		return Collections.emptyList();
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getNodeProperties(Object nodeObject) {
		try {
			final IEolContext ctx = module.getContext();
			final Operation opLabel = module.getOperations().getOperation(nodeObject, OP_PROPERTIES,
					Collections.emptyList(), ctx);
	
			if (opLabel == null) {
				EpsilonZestPlugin.getDefault()
						.logWarning("Object " + nodeObject + " has no " + OP_PROPERTIES + "() context operation");
			} else {
				Object rawResult = opLabel.execute(nodeObject, Collections.emptyList(), ctx);
				return (Map<String, Object>) rawResult;
			}
		} catch (Exception e) {
			EpsilonZestPlugin.getDefault().logException(e);
		}
		return Collections.emptyMap();
	}

	public String getNodeLabel(Object nodeObject) {
		try {
			final IEolContext ctx = module.getContext();
			final Operation opLabel = module.getOperations().getOperation(nodeObject, OP_NODE_LABEL,
					Collections.emptyList(), ctx);

			if (opLabel == null) {
				EpsilonZestPlugin.getDefault()
						.logWarning("Object " + nodeObject + " has no " + OP_NODE_LABEL + "() context operation");
				return "<NONE>";
			} else {
				return opLabel.execute(nodeObject, Collections.emptyList(), ctx) + "";
			}
		} catch (EolRuntimeException e) {
			EpsilonZestPlugin.getDefault().logException(e);
			return "<ERROR>";
		}
	}

	public Map<String, Iterable<Object>> getOutgoing(Object nodeObject) {
		try {
			final IEolContext ctx = module.getContext();
			final Operation opLabel = module.getOperations().getOperation(nodeObject, OP_OUTGOING,
					Collections.emptyList(), ctx);

			if (opLabel == null) {
				EpsilonZestPlugin.getDefault()
						.logWarning("Object " + nodeObject + " has no " + OP_OUTGOING + "() context operation");
				return Collections.emptyMap();
			} else {
				Object result = opLabel.execute(nodeObject, Collections.emptyList(), ctx);
				return adaptToStringIterableMap(result);
			}
		} catch (EolRuntimeException e) {
			EpsilonZestPlugin.getDefault().logException(e);
			return Collections.emptyMap();
		}
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getEdgeProperties(Object sourceObject, Object targetObject, String label) {
		try {
			final IEolContext ctx = module.getContext();
			final List<Object> parameters = Arrays.asList(label, targetObject);
			final Operation opLabel = module.getOperations().getOperation(sourceObject, OP_PROPERTIES,
					parameters, ctx);

			if (opLabel == null) {
				EpsilonZestPlugin.getDefault()
						.logWarning("Object " + sourceObject + " has no " + OP_PROPERTIES + "(String, Any) context operation");
				return Collections.emptyMap();
			} else {
				Object result = opLabel.execute(sourceObject, parameters, ctx);
				return (Map<String, Object>) result;
			}
		} catch (EolRuntimeException e) {
			EpsilonZestPlugin.getDefault().logException(e);
			return Collections.emptyMap();
		}
	}

	@SuppressWarnings("unchecked")
	protected Map<String, Iterable<Object>> adaptToStringIterableMap(Object result) {
		if (result instanceof Map) {
			Map<String, Object> map = (Map<String, Object>)result;

			// scan the map and adapt anything that is not iterable
			List<String> adaptedKeys = null;
			List<Iterable<Object>> adaptedValues = null;
			for (Iterator<Entry<String, Object>> itEntry = map.entrySet().iterator(); itEntry.hasNext(); ) {
				Entry<String, Object> entry = itEntry.next();
				if (entry.getValue() instanceof Iterable) {
					// nothing to do
				} else {
					if (adaptedKeys == null) {
						adaptedKeys = new ArrayList<>();
						adaptedValues = new ArrayList<>();
					}
					adaptedKeys.add(entry.getKey());
					adaptedValues.add(adaptToIterable(entry.getValue()));
					itEntry.remove();
				}
			}
			if (adaptedKeys != null) {
				for (int i = 0; i < adaptedKeys.size(); i++) {
					map.put(adaptedKeys.get(i), adaptedValues.get(i));
				}
			}

			return (Map<String, Iterable<Object>>) result;
		} else if (result instanceof Iterable) {
			return Collections.singletonMap("out", (Iterable<Object>)result);
		} else {
			return Collections.singletonMap("out", Collections.singletonList(result));
		}
	}

	@SuppressWarnings("unchecked")
	protected Iterable<Object> adaptToIterable(Object result) {
		if (result instanceof Iterable) {
			return (Iterable<Object>) result;
		} else {
			return Collections.singleton(result);
		}
	}

}
