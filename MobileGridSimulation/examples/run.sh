#simulation configuration file
SIMULATION_CONF_FILE=SEAS-2A100-shortJobs.cnf

echo "Configured simulation: $SIMULATION_CONF_FILE"
echo "Running simulation..."

java -jar RunnableSimulator.jar $SIMULATION_CONF_FILE > output/output_$SIMULATION_CONF_FILE

echo "Simulation output was saved in output/output_$SIMULATION_CONF_FILE"