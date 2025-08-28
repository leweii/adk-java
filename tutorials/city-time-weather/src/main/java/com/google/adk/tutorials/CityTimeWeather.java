/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.adk.tutorials;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.events.Event;
import com.google.adk.planners.PlanReActPlanner;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.adk.tools.Annotations.Schema;
import com.google.adk.tools.FunctionTool;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class CityTimeWeather {

  public static final BaseAgent ROOT_AGENT =
      LlmAgent.builder()
          .name("multi_tool_agent")
          .model("gemini-2.0-flash-lite")
          .description("Agent to answer questions about math.")
          .planner(new PlanReActPlanner())
          .instruction(
              "You are a helpful agent who can answer user questions about math. you can only use the given tools.")
          .tools(
              FunctionTool.create(CityTimeWeather.class, "randomInt"),
              FunctionTool.create(CityTimeWeather.class, "multiply"),
              FunctionTool.create(CityTimeWeather.class, "guagua"),
              FunctionTool.create(CityTimeWeather.class, "plus")
          )
          .build();

  public static Map<String, String> multiply(
      @Schema(
          description = "multiple given integers")
      Integer x, Integer y) {
    return Map.of(
        "status", "success", "integer", "" + Math.multiplyExact(x, y));
  }
  public static Map<String, String> guagua(
      @Schema(
          description = "guagua given integers")
      Integer x, Integer y) {
    return Map.of(
        "status", "success", "integer", "" + Math.subtractExact(x, y));
  }

  public static Map<String, String> plus(
      @Schema(
          description = "plus given integers")
      Integer x, Integer y) {
    return Map.of(
        "status", "success", "integer", "" + Math.addExact(x, y));
  }

  public static Map<String, String> randomInt() {
    Random random = new Random();
    return Map.of(
        "status", "success", "integer", "" + random.nextInt(100));
  }

  private static String USER_ID = "student";
  private static String NAME = "multi_tool_agent";

  public static void main(String[] args) throws Exception {
    InMemoryRunner runner = new InMemoryRunner(ROOT_AGENT);

    Session session =
        runner
            .sessionService()
            .createSession(NAME, USER_ID)
            .blockingGet();

    try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
      while (true) {
        System.out.print("\nYou > ");
        String userInput = scanner.nextLine();

        if ("quit".equalsIgnoreCase(userInput)) {
          break;
        }

        Content userMsg = Content.fromParts(Part.fromText(userInput));
        Flowable<Event> events = runner.runAsync(USER_ID, session.id(), userMsg);

        System.out.print("\nAgent > ");
        events.blockingForEach(event -> System.out.println(event.stringifyContent()));
      }
    }
  }
}
