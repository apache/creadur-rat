    ///////////////////////// Start common Arg manipulation code

    /**
     * A map of CLI based arguments to values.
     */
    protected final Map<String, List<String>> args = new HashMap<>();

    /**
     * Gets the list of arguments prepared for the CLI code to parse.
     * @return the List of arguments for the CLI command line.
     */
    protected List<String> args() {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : args.entrySet()) {
            result.add("--"+entry.getKey());
            result.addAll(entry.getValue().stream().filter(Objects::nonNull).collect(Collectors.toList()));
        }
        return result;
    }

    /**
     * Set a key and value into the argument list.
     * Replaces any existing value.
     * @param key the key for the map.
     * @param value the value to set.
     */
    protected void setArg(String key, String value) {
        List<String> values = new ArrayList<>();
        values.add(value);
        args.put(key, values);
    }

    /**
     * Get the list of values for a key.
     * @param key the key for the map.
     * @return the list of values for the key or {@code null} if not set.
     */
    public List<String> getArg(String key) {
        return args.get(key);
    }

    /**
     * Add a value to the key in the argument list.
     * If the key does not exist, adds it.
     * @param key the key for the map.
     * @param value the value to set.
     */
    protected void addArg(String key, String value) {
        List<String> values = args.get(key);
        if (values == null) {
            setArg(key, value);
        } else {
            values.add(value);
        }
    }

    /**
     * Add an value to the key in the argument list.
     * If the key does not exist, adds it.
     * @param key the key for the map.
     * @param value the value to set.
     */
    protected void addArg(String key, String[] value) {
        List<String> values = args.get(key);
        if (values == null) {
            values = new ArrayList<>();
            args.put(key, values);
        }
        values.addAll(Arrays.asList(value));
    }

    /**
     * remove a key from the argument list.
     * @param key the key to remove from the map.
     */
    protected void removeArg(String key) {
        args.remove(key);
    }

 ///////////////////////// End common Arg manipulation code
