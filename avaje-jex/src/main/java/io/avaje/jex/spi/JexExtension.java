package io.avaje.jex.spi;

import io.avaje.jex.TemplateRender;
import io.avaje.spi.Service;

@Service
public sealed interface JexExtension permits JsonService, TemplateRender {}
