/*
 * Copyright (c) 2017. Uber Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.uber.autodispose;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.functions.Function;

/**
 * Entry point for auto-disposing {@link Flowable}s.
 * <p>
 * The basic flow stencil might look like this:
 * <pre><code>
 *   myThingFlowable
 *        .to(new FlowableScoper<Thing>(...))
 *        .subscribe(...)
 * </code></pre>
 * <p>
 * There are several constructor overloads, with the most basic being a simple {@link
 * #FlowableScoper(Maybe)}. The provided {@link Maybe} is ultimately what every scope resolves to
 * under the hood, and AutoDispose has some built-in understanding for predefined types. The scope
 * is considered ended upon onSuccess emission of this {@link Maybe}. The most common use case would
 * probably be {@link #FlowableScoper(ScopeProvider)}.
 *
 * @param <T> the stream type.
 * @deprecated Use the static factories in {@link AutoDispose}. This will be removed in 1.0.
 */
@Deprecated
public class FlowableScoper<T> extends BaseAutoDisposeConverter
    implements Function<Flowable<? extends T>, FlowableSubscribeProxy<T>> {

  public FlowableScoper(ScopeProvider provider) {
    super(provider);
  }

  public FlowableScoper(Maybe<?> lifecycle) {
    super(lifecycle);
  }

  @Override public FlowableSubscribeProxy<T> apply(final Flowable<? extends T> source)
      throws Exception {
    return source
        .map(BaseAutoDisposeConverter.<T>identityFunctionForGenerics())
        .as(AutoDispose.<T>autoDisposable(scope()));
  }
}

