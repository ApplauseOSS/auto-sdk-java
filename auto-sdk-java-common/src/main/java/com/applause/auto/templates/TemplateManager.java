/*
 *
 * Copyright © 2024 Applause App Quality, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.applause.auto.templates;

import com.google.common.hash.Hashing;
import freemarker.cache.StringTemplateLoader;
import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateNotFoundException;
import freemarker.template.Version;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

/** Helper class to build and evaluate FreeMarker templates */
public final class TemplateManager {
  private static final Configuration freemarkerConfig;
  // Freemarker versioning.  This is how they handle non-backward compatible changes
  // We chose 2.3.30 because that's the version we first released with (so when we move to future
  // versions of Freemarker, we do NOT change this value until we understand of we are going to have
  // non-backward compatible changes
  static final Version freemarkerVersion = new Version(2, 3, 30);

  static {
    freemarkerConfig = new Configuration(freemarkerVersion);
    freemarkerConfig.setDefaultEncoding("UTF-8");
    freemarkerConfig.setLocale(Locale.getDefault());
    freemarkerConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
  }

  private TemplateManager() {}

  /**
   * Process a template with the current environment. Uses a hashed template name generated from
   * template contents
   *
   * @param templateContents The template contents
   * @return the freemarker template
   * @throws TemplateGenerationException If the template could not be generated
   */
  public static Template generateTemplate(final String templateContents)
      throws TemplateGenerationException {
    final var templateContentHash =
        Hashing.goodFastHash(128).hashString(templateContents, StandardCharsets.UTF_8).toString();
    return generateTemplate(templateContentHash, templateContents, freemarkerConfig);
  }

  /**
   * Process a template with the current environment
   *
   * @param name The name of the template. Used in error messages
   * @param templateContents The template contents
   * @return the freemarker template
   * @throws TemplateGenerationException If template generation fails
   */
  public static Template generateTemplate(final String name, final String templateContents)
      throws TemplateGenerationException {
    return generateTemplate(name, templateContents, freemarkerConfig);
  }

  /**
   * Process a freemarker template with a given set of environment variables
   *
   * @param name the name of the template. Used if we have to print an error message
   * @param templateContents The contents of a template
   * @param cfg The freemarker configuration
   * @return the freemarker template
   * @throws TemplateGenerationException If template generation fails
   */
  public static Template generateTemplate(
      final String name, final String templateContents, final Configuration cfg)
      throws TemplateGenerationException {
    StringTemplateLoader stl = new StringTemplateLoader();
    stl.putTemplate(name, templateContents);
    cfg.setTemplateLoader(stl);
    return generateTemplate(name, cfg);
  }

  /**
   * Process a specific template in a Freemarker config
   *
   * @param name The name of the template to process
   * @param cfg The Freemarker configuration
   * @return The freemarker template
   * @throws TemplateGenerationException If template generation fails
   */
  public static Template generateTemplate(final String name, final Configuration cfg)
      throws TemplateGenerationException {
    Template template;
    try {
      template = cfg.getTemplate(name);
      template.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
      template.setLogTemplateExceptions(false);
    } catch (ParseException pe) {
      throw new TemplateGenerationException(
          String.format(
              "Template %s line=%d column=%d Parse error: %s",
              pe.getTemplateName(), pe.getLineNumber(), pe.getColumnNumber(), pe.getMessage()),
          pe);
    } catch (MalformedTemplateNameException badTemplateEx) {
      throw new TemplateGenerationException(
          String.format(
              "Template %s has a Malformed Name: %s",
              badTemplateEx.getTemplateName(), badTemplateEx.getMalformednessDescription()),
          badTemplateEx);
    } catch (TemplateNotFoundException notFoundEx) {
      throw new TemplateGenerationException(
          String.format(
              "Template %s not found: %s", notFoundEx.getTemplateName(), notFoundEx.getMessage()),
          notFoundEx);
    } catch (IOException ioe) {
      throw new TemplateGenerationException(
          String.format("Unable to read Template %s: %s", name, ioe.getMessage()), ioe);
    }
    return template;
  }

  /**
   * Processes a template with a set of provided properties
   *
   * @param template The template
   * @param properties The set of properties
   * @return The template string after substitutions
   * @throws TemplateProcessException If processing fails
   */
  public static String process(final Template template, final Map<String, Object> properties)
      throws TemplateProcessException {
    final StringWriter out = new StringWriter();
    try {
      template.process(properties, out);
    } catch (TemplateException te) {
      // There's a lot of data in the message.  Take the first two lines
      final String msg = te.getMessage();
      String[] splitMsg = msg.split("\n");
      StringBuilder sb = new StringBuilder();
      if (splitMsg.length >= 1) {
        sb.append(splitMsg[0].trim());
      }
      if (splitMsg.length >= 2) {
        sb.append(String.format(" %s", splitMsg[1].trim()));
      }
      throw new TemplateProcessException(
          String.format(
              "Template %s line=%d column=%d Template error: %s",
              te.getTemplateSourceName(), te.getLineNumber(), te.getColumnNumber(), sb),
          te);
    } catch (IOException ioe) {
      throw new TemplateProcessException("Unable to process template " + template.getName(), ioe);
    }
    return out.toString();
  }

  /** Exception to signal when a template fails to be generated */
  public static final class TemplateGenerationException extends Exception {
    /**
     * Create a new TemplateGeneration Exception wrapping the underlying Throwable
     *
     * @param message The message for this exception
     * @param cause The Throwable that caused this exception
     */
    public TemplateGenerationException(final String message, final Throwable cause) {
      super(message, cause);
    }
  }

  /** Exception to signal when a template fails to be processed */
  public static final class TemplateProcessException extends Exception {

    /**
     * Create a new TemplateProcessException Exception wrapping the underlying Throwable
     *
     * @param message The message for this exception
     * @param cause The Throwable that caused this exception
     */
    public TemplateProcessException(final String message, final Throwable cause) {
      super(message, cause);
    }
  }
}
