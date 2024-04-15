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
package com.applause.auto.helpers;

import com.applause.auto.context.FrameworkContext;
import com.applause.auto.framework.ContextBuilder;
import com.applause.auto.helpers.email.Email;
import com.applause.auto.helpers.email.Inbox;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import lombok.SneakyThrows;
import org.testng.Assert;
import org.testng.annotations.Test;

public class EmailHelperTest {

  @Test
  @SneakyThrows
  public void testEmailParsing() {
    final var context = ContextBuilder.setup().get();
    TestInbox inbox = new TestInbox(context);
    final var res = inbox.waitForEmail(Duration.ZERO);
    Assert.assertTrue(res.getBody().startsWith("This is the content"));
    Assert.assertEquals(res.getAttachments().size(), 1);
  }

  class TestInbox extends Inbox {

    public TestInbox(final FrameworkContext ctx) {
      super(ctx, "fake-address");
    }

    @Override
    public Email waitForEmail(Duration timeout) throws TimeoutException {

      Message msg;
      try (InputStream stream = new FileInputStream("src/test/resources/test.eml")) {
        msg = new MimeMessage(null, stream);
      } catch (IOException e) {
        throw new RuntimeException("Unable to download .eml file.", e);
      } catch (MessagingException e) {
        throw new RuntimeException("Unable to parse .eml file.", e);
      }

      return new Email(msg);
    }
  }
}
