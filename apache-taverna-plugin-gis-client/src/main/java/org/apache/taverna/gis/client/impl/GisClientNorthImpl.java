/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.taverna.gis.client.impl;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.taverna.gis.client.ComplexDataFormat;
import org.apache.taverna.gis.client.ComplexPortDataDescriptor;
import org.apache.taverna.gis.client.IGisClient;
import org.apache.taverna.gis.client.IPortDataDescriptor;
import org.apache.taverna.gis.client.PortDataDescriptorFactory;
import org.n52.wps.client.ExecuteRequestBuilder;
import org.n52.wps.client.ExecuteResponseAnalyser;
import org.n52.wps.client.WPSClientException;
import org.n52.wps.client.WPSClientSession;

import net.opengis.ows.x11.LanguageStringType;
import net.opengis.wps.x100.CapabilitiesDocument;
import net.opengis.wps.x100.DataType;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.OutputDataType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessBriefType;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType.DataInputs;
import net.opengis.wps.x100.WPSCapabilitiesType;
import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ExecuteResponseDocument;

public class GisClientNorthImpl implements IGisClient {

	private Logger logger = Logger.getLogger(GisClientNorthImpl.class);

	private URI serviceURI = null;
	private WPSClientSession wpsClient;

	public GisClientNorthImpl(String serviceURL) {
		this.serviceURI = URI.create(serviceURL);
		wpsClient = WPSClientSession.getInstance();

		try {
			wpsClient.connect(serviceURI.toString());
		} catch (WPSClientException ex) {
			logger.error("Failed to connect to service: " + serviceURI, ex);
		}

	}

	@Override
	public String getServiceCapabilities(URI serviceURI) {

		CapabilitiesDocument capabilities = wpsClient.getWPSCaps(serviceURI.toString());

		LanguageStringType[] serviceAbstract = capabilities.getCapabilities().getServiceIdentification()
				.getTitleArray();

		if (serviceAbstract != null && serviceAbstract.length > 0)
			return serviceAbstract[0].getStringValue();
		else
			return null;
	}

	@Override
	public HashMap<String, Integer> getProcessInputPorts(String processID) {
		HashMap<String, Integer> inputPorts = new HashMap<String, Integer>();

		ProcessDescriptionType processDescription = null;

		try {
			processDescription = wpsClient.getProcessDescription(serviceURI.toString(), processID);
		} catch (IOException ex) {
			logger.error("Failed to list input ports for process: " + processID, ex);
		}

		if (processDescription == null)
			return inputPorts;

		DataInputs dataInputs = processDescription.getDataInputs();

		if (dataInputs == null)
			return inputPorts;

		InputDescriptionType[] inputList = dataInputs.getInputArray();

		for (InputDescriptionType input : inputList) {

			// if compareTo returns 1 then first value is greater than 1. it
			// means that there is more than one occurrence therefore the depth
			// is more than 0
			int depth = ((input.getMaxOccurs().compareTo(BigInteger.valueOf(1)) == 1) ? 1 : 0);

			inputPorts.put(input.getIdentifier().getStringValue(), depth);
		}

		return inputPorts;

	}

	@Override
	public HashMap<String, Integer> getProcessOutputPorts(String processID) {
		HashMap<String, Integer> outputPorts = new HashMap<String, Integer>();

		ProcessDescriptionType processDescription = null;

		try {
			processDescription = wpsClient.getProcessDescription(serviceURI.toString(), processID);
		} catch (IOException ex) {
			logger.error("Failed to list processe output port for process: " + processID, ex);
		}

		if (processDescription == null)
			return outputPorts;

		OutputDescriptionType[] outputList = processDescription.getProcessOutputs().getOutputArray();

		for (OutputDescriptionType output : outputList) {

			// TODO: Calculate output depth
			int depth = 0;

			outputPorts.put(output.getIdentifier().getStringValue(), depth);
		}

		return outputPorts;
	}

	public List<IPortDataDescriptor> getTaverna2InputPorts(String processID) {

		List<IPortDataDescriptor> inputPorts = new ArrayList<IPortDataDescriptor>();

		ProcessDescriptionType processDescription = null;

		try {
			processDescription = wpsClient.getProcessDescription(serviceURI.toString(), processID);
		} catch (IOException ex) {
			logger.error("Failed to get process description for process: " + processID, ex);
		}

		if (processDescription == null)
			return inputPorts;

		DataInputs dataInputs = processDescription.getDataInputs();

		if (dataInputs == null)
			return inputPorts;

		InputDescriptionType[] inputList = dataInputs.getInputArray();

		for (InputDescriptionType input : inputList) {
			IPortDataDescriptor myNewInputPort = PortDataDescriptorFactory.getInstance().getPortDataDescriptor(input);

			inputPorts.add(myNewInputPort);
		}

		return inputPorts;
	}

	@Override
	public List<IPortDataDescriptor> getTaverna2OutputPorts(String processID) {
		List<IPortDataDescriptor> outputPorts = new ArrayList<IPortDataDescriptor>();

		ProcessDescriptionType processDescription = null;

		try {
			processDescription = wpsClient.getProcessDescription(serviceURI.toString(), processID);
		} catch (IOException ex) {
			logger.error("Failed to list output ports for process: " + processID, ex);
		}

		if (processDescription == null)
			return outputPorts;

		OutputDescriptionType[] outputList = processDescription.getProcessOutputs().getOutputArray();

		for (OutputDescriptionType output : outputList) {
			IPortDataDescriptor myNewOutputPort = PortDataDescriptorFactory.getInstance().getPortDataDescriptor(output);
			outputPorts.add(myNewOutputPort);
		}

		return outputPorts;
	}

	@Override
	public List<String> getProcessList() {
		List<String> results = new ArrayList<String>();

		WPSCapabilitiesType wpsCapabilities = wpsClient.getWPSCaps(serviceURI.toString()).getCapabilities();

		ProcessBriefType[] processList = wpsCapabilities.getProcessOfferings().getProcessArray();

		for (ProcessBriefType process : processList) {
			results.add(process.getIdentifier().getStringValue());
		}

		return results;

	}

	@Override
	public HashMap<String, String> executeProcess(String processID, HashMap<String, IPortDataDescriptor> inputs)
			throws Exception {

		HashMap<String, String> executeOutput = new HashMap<String, String>();

		ProcessDescriptionType processDescription = null;

		// Get process description
		try {
			processDescription = wpsClient.getProcessDescription(serviceURI.toString(), processID);
		} catch (IOException ex) {
			throw new Exception("Failed to get process description for process: " + processID, ex);
		}

		// Initialise execute builder
		ExecuteRequestBuilder executeBuilder = new ExecuteRequestBuilder(processDescription);

		boolean hasInput = true;

		DataInputs dataInputs = processDescription.getDataInputs();

		if (dataInputs == null)
			hasInput = false;

		if (hasInput) {

			InputDescriptionType[] inputList = dataInputs.getInputArray();

			// TODO: Handle input when depth > 0
			// Provide user values for each service input
			for (InputDescriptionType input : inputList) {
				String inputName = input.getIdentifier().getStringValue();
				Object inputValue = inputs.containsKey(inputName)?inputs.get(inputName).getValue():null;

				// Check if input is required but not provided
				if (inputValue == null && input.getMinOccurs().intValue() > 0) {
					throw new IOException("Required Input not set: " + inputName);
				}

				// Skip not user supplied optional inputs
				if (inputValue!=null)
				{
					if (input.getLiteralData() != null) {
						if (inputValue instanceof String) {
							executeBuilder.addLiteralData(inputName, (String) inputValue);
						}
	
					} else if (input.getComplexData() != null) {
	
						// Check if the selected format (mimeType, encoding, schema)
						// is supported by the service
						ComplexPortDataDescriptor complexData = (ComplexPortDataDescriptor) inputs.get(inputName);
						ComplexDataFormat selectedFormat = complexData.getComplexFormat();
	
						if (!complexData.getSupportedComplexFormats().contains(selectedFormat)) {
							throw new IllegalArgumentException(
									"Unsupported format: " + complexData.getComplexFormat().toString());
						}
	
						if (inputValue instanceof String) {
							// Check if complex data is provided by reference or by
							// value
							boolean isReference = true;
	
							try {
								URI.create((String) inputValue);
							} catch (IllegalArgumentException ex) {
								isReference = false;
							}
	
							if (isReference) {
								// complex data by reference
								executeBuilder.addComplexDataReference(inputName, (String) inputValue,
										selectedFormat.getSchema(), selectedFormat.getEncoding(),
										selectedFormat.getMimeType());
							} else {
	
								// complex data by value
								try {
	
									executeBuilder.addComplexData(inputName, (String) inputValue,
											selectedFormat.getSchema(), selectedFormat.getEncoding(),
											selectedFormat.getMimeType());
	
								} catch (WPSClientException e) {
									throw new Exception("Failed to set complex data: " + processID, e);
								}
							}
						}
					} else if (input.getBoundingBoxData() != null) {
						// TODO: Handle BBox data
					}
				}
				

			} // End input loop

		}
		ExecuteDocument execute = executeBuilder.getExecute();

		execute.getExecute().setService("WPS");

		Object responseObject = null;

		try {
			// Execute service
			responseObject = wpsClient.execute(serviceURI.toString(), execute);
		} catch (WPSClientException e) {
			throw new Exception(e.getServerException().xmlText());
		}

		// Register outputs
		if (responseObject instanceof ExecuteResponseDocument) {
			ExecuteResponseDocument response = (ExecuteResponseDocument) responseObject;

			// analyser is used to get complex data
			ExecuteResponseAnalyser analyser = new ExecuteResponseAnalyser(execute, response, processDescription);

			for (OutputDataType output : response.getExecuteResponse().getProcessOutputs().getOutputArray()) {
				DataType data = output.getData();

				if (data.isSetLiteralData()) {

					// simpleRef =
					// referenceService.register(data.getLiteralData().getStringValue(),
					// 0, true, context);

					executeOutput.put(output.getIdentifier().getStringValue(), data.getLiteralData().getStringValue());
				} else {

					// simpleRef =
					// referenceService.register(data.getComplexData().toString(),
					// 0, true, context);

					executeOutput.put(output.getIdentifier().getStringValue(), data.getComplexData().toString());

				}

			}

		}

		return executeOutput;
	}
}
