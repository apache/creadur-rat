    ///////////////////////// Start common Arg manipulation code

    /**
     * Sets the deprecation report method.
     */
    private static void setDeprecationReporter() {
        DeprecationReporter.setLogReporter(opt -> {
            String msg = deprecatedArgs.get(argsKey(opt));
            if (msg == null) {
                DeprecationReporter.getDefault().accept(opt);
            } else {
                DefaultLog.getInstance().warn(msg);
            }
        });
    }

    private static String argsKey(Option opt) {
        return StringUtils.defaultIfEmpty(opt.getLongOpt(), opt.getKey());
    }

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
            result.add("--" + entry.getKey());
            result.addAll(entry.getValue().stream().filter(Objects::nonNull).collect(Collectors.toList()));
        }
        return result;
    }

    private boolean validateSet(String key) {
        Arg arg = Arg.findArg(key);
        if (arg != null) {
            Option opt = arg.find(key);
            Option main = arg.option();
            if (opt.isDeprecated()) {
                args.remove(argsKey(main));
                // deprecated options must be explicitly set so let it go.
                return true;
            }
            // non-deprecated options may have default so ignore it if another option has already been set.
            for (Option o : arg.group().getOptions()) {
                if (!o.equals(main)) {
                    if (args.containsKey(argsKey(o))) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Set a key and value into the argument list.
     * Replaces any existing value.
     * @param key the key for the map.
     * @param value the value to set.
     */
    protected void setArg(String key, String value) {
        if (value == null || StringUtils.isNotBlank(value)) {
            if (validateSet(key)) {
                List<String> values = new ArrayList<>();
                if (DefaultLog.getInstance().isEnabled(Log.Level.DEBUG)) {
                    DefaultLog.getInstance().debug(String.format("Setting %s to '%s'", key, value));
                }
                values.add(value);
                args.put(key, values);
            }
        }
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
     * Add values to the key in the argument list.
     * empty values are ignored.  If no non-empty values are present no change is made.
     * If the key does not exist, adds it.
     * @param key the key for the map.
     * @param value the array of values to set.
     */
    protected void addArg(String key, String[] value) {
        List<String> newValues = Arrays.stream(value).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        if (!newValues.isEmpty()) {
            if (validateSet(key)) {
                if (DefaultLog.getInstance().isEnabled(Log.Level.DEBUG)) {
                    DefaultLog.getInstance().debug(String.format("Adding [%s] to %s", String.join(", ", Arrays.asList(value)), key));
                }
                List<String> values = args.get(key);
                if (values == null) {
                    values = new ArrayList<>();
                    args.put(key, values);
                }
                values.addAll(newValues);
            }
        }
    }
    
    /**
     * Add a value to the key in the argument list.
     * If the key does not exist, adds it.
     * @param key the key for the map.
     * @param value the value to set.
     */
    protected void addArg(String key, String value) {
        if (StringUtils.isNotBlank(value)) {
            if (validateSet(key)) {
                List<String> values = args.get(key);
                if (DefaultLog.getInstance().isEnabled(Log.Level.DEBUG)) {
                    DefaultLog.getInstance().debug(String.format("Adding [%s] to %s", String.join(", ", Arrays.asList(value)), key));
                }
                if (values == null) {
                    values = new ArrayList<>();
                    args.put(key, values);
                }
                values.add(value);
            }
        }
    }

    /**
     * Remove a key from the argument list.
     * @param key the key to remove from the map.
     */
    protected void removeArg(String key) {
        args.remove(key);
    }

 ///////////////////////// End common Arg manipulation code
