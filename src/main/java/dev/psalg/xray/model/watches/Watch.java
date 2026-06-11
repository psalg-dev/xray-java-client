package dev.psalg.xray.model.watches;

/** An Xray watch: a named binding between resources (repositories, builds, or projects) and policies. */
public record Watch(
        String name,
        String description,
        boolean active
) {}
