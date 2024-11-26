package io.avaje.jex.spi;

import io.avaje.spi.Service;

/**
 * Extension point for all Jex SPI interfaces
 *
 * <p>All types that implement this interface must be registered as an entry in {@code
 * META-INF/services/io.avaje.jex.spi.JexExtension } for it to be loaded by Jex
 */
@Service
public sealed interface JexExtension permits JsonService, TemplateRender {}
