/******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.exactpro.sf.aml.script;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.exactpro.sf.common.util.EPSCommonException;


public class MetaContainer {

	private final Map<String, Double> doublePrecision = new HashMap<>();
	private final Map<String, Double> systemPrecision = new HashMap<>();
    private final Map<String, Object> systemColumns = new HashMap<>();
	private String alternateValue;
    private String failUnexpected;

    private final Map<String, List<MetaContainer>> children;

	private void propogateParentValues(MetaContainer parentMetaContainer)
	{
		Map<String, Double> parentDoublePrecision = parentMetaContainer.getDoublePrecision();
		Map<String, Double> parentSystemPrecision = parentMetaContainer.getSystemPrecision();

		for(Entry<String, Double> entry: parentDoublePrecision.entrySet()) {
			if (!doublePrecision.containsKey(entry.getKey())) {
				doublePrecision.put(entry.getKey(), entry.getValue());
			}
		}
		for(Entry<String, Double> entry: parentSystemPrecision.entrySet()) {
			if (!systemPrecision.containsKey(entry.getKey())) {
				systemPrecision.put(entry.getKey(), entry.getValue());
			}
		}

        if(failUnexpected == null) {
            failUnexpected = parentMetaContainer.getFailUnexpected();
        }

		for (Entry<String, List<MetaContainer>> fldChl : children.entrySet()) {
			for (MetaContainer child: fldChl.getValue()) {
				child.propogateParentValues(this);
			}
		}
	}

	public MetaContainer() {
		this.children = new HashMap<>();
	}

	private Map<String, Double> getDoublePrecision() {
        return doublePrecision;
	}
	public Double getDoublePrecision(String field) {
        return doublePrecision != null ? doublePrecision.get(field) : null;
    }

	public void addDoublePrecision(String doublePrecision) {
		this.doublePrecision.putAll(parsePrecision(doublePrecision));
	}

	private Map<String, Double> getSystemPrecision() {
        return systemPrecision;
	}
	public Double getSystemPrecision(String field) {
        return systemPrecision != null ? systemPrecision.get(field) : null;
    }

	public void addSystemPrecision(String systemPrecision) {
		this.systemPrecision.putAll(parsePrecision(systemPrecision));
	}

	public String getAlternateValue() {
		return alternateValue;
	}

	public void setAlternateValue(String alternateValue) {
		this.alternateValue = alternateValue;
	}

	public Map<String, List<MetaContainer>> getChildren() {
		return children;
	}

	public void add(String name, MetaContainer mc) {
        List<MetaContainer> obj = children.computeIfAbsent(name, it -> new ArrayList<>());
        mc.propogateParentValues(this);
		obj.add(mc);
	}

	public List<MetaContainer> get(String name) {
		return children.get(name);
	}

	private Map<String, Double> parsePrecision(String precision)
	{
        if(StringUtils.isEmpty(precision)) {
			return Collections.emptyMap();
		}
		Map<String, Double> fieldVal = new HashMap<>();
		String[] precs = precision.split(";");
		for (String prec : precs)
		{
			String[] arr = prec.trim().split("=");
			String field = arr[0].trim();
            if(field.isEmpty()) {
				throw new EPSCommonException("Parameter name not specified: "+precision);
			}
			if (arr.length == 1) {
				throw new EPSCommonException("Precision for parameter not specified: "+field);
			}
			try {
				Double value = Double.parseDouble(arr[1].trim());
				fieldVal.put(field, value);
			} catch (NumberFormatException e) {
				throw new EPSCommonException("Precision is not in double format: "+arr[1].trim());
			}
		}
		return fieldVal;
	}


	public String getFailUnexpected() {
		return failUnexpected;
	}

	public void setFailUnexpected(String failUnexpected) {
		this.failUnexpected = failUnexpected;
	}

    @SuppressWarnings("unchecked")
    public <T> T getSystemColumn(String name) {
        return (T)systemColumns.get(name);
    }

    public void putSystemColumn(String name, Object value) {
        systemColumns.put(name, value);
    }

	/**
	 * Create clone without children
	 */
	@Override
    public MetaContainer clone() {
        return clone(false);
    }

    public MetaContainer clone(boolean withChildren) {
        MetaContainer result = new MetaContainer();

        result.setFailUnexpected(this.failUnexpected);
        result.setAlternateValue(this.alternateValue);
        result.doublePrecision.putAll(this.doublePrecision);
        result.systemPrecision.putAll(this.systemPrecision);
        result.systemColumns.putAll(this.systemColumns);

        if(withChildren) {
            children.forEach((name, list) -> {
                for(MetaContainer child : list) {
                    result.add(name, child.clone(true));
                }
            });
        }

        return result;
    }

	@Override
	public String toString() {
		return new ToStringBuilder(this)
		.append("failUnexpected", failUnexpected)
                .append("alternateValue", alternateValue)
                .append("doublePrecision", doublePrecision)
                .append("children", children).toString();
	}
}
