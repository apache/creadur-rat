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
     * A map of CLI-based arguments to values.
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
            result.addAll(entry.getValue().stream().filter(Objects::nonNull).toList());
        }
        return result;
    }

    private boolean validateSet(String key) {
        Arg arg = Arg.findArg(key);
        if (arg != null) {
            Option opt = arg.find(key);
            Option main = arg.option();
            if (opt.hasArgs()) {
                // a multi-valued option accumulates values from every alias of its group, deprecated or not,
                // into the key returned by storageKey(), so there is nothing to conflict with here.
                return true;
            }
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
     * Returns the key under which the value(s) for {@code key} are actually stored. An option that accepts
     * multiple arguments is stored under the key of the non-deprecated option of its group, regardless of which
     * alias {@code key} names, so that the deprecated and non-deprecated spellings of the same multi-valued
     * option (for instance {@code --exclude} and {@code --input-exclude}) accumulate their values together
     * instead of one silently discarding the other's (RAT-552); the final argument list built by {@link #args()}
     * then names each such option only once, as commons-cli requires for options of the same group. Any other
     * option is stored under its own key, as passed in.
     * @param key the key as passed to {@link #setArg(String, String)} or {@link #addArg(String, String[])}.
     * @return the key under which the value(s) are stored in {@link #args}.
     */
    private String storageKey(String key) {
        Arg arg = Arg.findArg(key);
        if (arg != null) {
            Option opt = arg.find(key);
            if (opt.hasArgs()) {
                return argsKey(arg.option());
            }
        }
        return key;
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
                args.put(storageKey(key), values);
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
     * empty values are ignored. If no non-empty values are present no change is made.
     * If the key does not exist, adds it.
     * @param key the key for the map.
     * @param value the array of values to set.
     */
    protected void addArg(String key, String[] value) {
        List<String> newValues = Arrays.stream(value).filter(StringUtils::isNotBlank).toList();
        if (!newValues.isEmpty()) {
            if (validateSet(key)) {
                if (DefaultLog.getInstance().isEnabled(Log.Level.DEBUG)) {
                    DefaultLog.getInstance().debug(String.format("Adding [%s] to %s", String.join(", ", Arrays.asList(value)), key));
                }
                List<String> values = args.computeIfAbsent(storageKey(key), k -> new ArrayList<>());
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
                String storageKey = storageKey(key);
                List<String> values = args.get(storageKey);
                if (DefaultLog.getInstance().isEnabled(Log.Level.DEBUG)) {
                    DefaultLog.getInstance().debug(String.format("Adding [%s] to %s", String.join(", ", Arrays.asList(value)), key));
                }
                if (values == null) {
                    values = new ArrayList<>();
                    args.put(storageKey, values);
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
