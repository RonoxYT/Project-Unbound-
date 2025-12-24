Here is the Technical Development Report (DevLog) 

📂 Project Unbound: Incident Report & Technical Solutions
1. The 20 FPS Lock (Involuntary V-Sync)
🔴 Problem: The game launched, but FPS was hard-locked at 20, identical to the internal TPS.

🔍 Cause: The render call (render) was trapped inside the logic loop (tick). Since the game ticks 20 times per second, it was only drawing 20 frames per second.

✅ Solution: Implementation of the "Decoupled Loop". Total separation: logic runs at a fixed 20Hz, while rendering runs in a free loop (unlimited).

3. The "Spiral of Death" (Speed Hack Effect)
🔴 Problem: After a loading screen or a lag spike, the game would accelerate wildly (fast-forward motion) to catch up on lost time.

🔍 Cause: The Scheduler was accumulating lag time. If the game froze for 2 seconds, the engine attempted to execute 40 ticks instantly upon resumption.

✅ Solution: Added a Safety Cap. If the accumulated lag exceeds 100ms, the engine "forgets" the past and resumes normal flow, preventing the overload spiral.

4. Double Ticking (2x Speed)
🔴 Problem: Despite fixes, the game appeared to run twice as fast as normal.

🔍 Cause: Calling MinecraftClient.render(true) triggered another update of Mojang's internal clock. Combined with our custom Scheduler, the game was advancing 2 steps every frame.

✅ Solution (Attempt 1): Switched to render(false) to prevent Mojang from touching the time.

5. The Interpolation Bug (Stuttering Movement)
🔴 Problem: With render(false), FPS were high, but entities appeared to "vibrate" or move in visual slow motion/stop-motion.

🔍 Cause: Disabling Mojang's internal tick also disabled the deltaTick calculation (interpolation). The game was rendering entities at their previous tick position without smoothing movement between frames.

✅ Final Solution: The "Tick Guard". We returned to render(true) for fluid motion, but injected a boolean lock (unbound_allowTick) into the tick() method. Only our Scheduler holds the key to authorize the game to advance logic.

6. The Multithreading Deadlock (Parallel Failure)
🔴 Problem: Attempted to calculate entity physics on multiple threads (Parallel Ticking). Result: Entities froze in place and chunks stopped loading.

🔍 Cause: Deadlock. Worker Threads were waiting for the Main Thread (to load chunks/collisions), while the Main Thread was waiting for the Worker Threads to finish their tasks.

✅ Solution: Abandoned raw parallelism for physics. Replaced it with the D.A.B. (Lobotomy) system: instead of calculating faster, we calculate less by disabling distant entities. This proved to be far more stable and efficient.
