/*
 * Copyright (C) 2019. Uber Technologies
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
package com.uber.autodispose;

import io.reactivex.rxjava3.core.CompletableConverter;
import io.reactivex.rxjava3.core.FlowableConverter;
import io.reactivex.rxjava3.core.MaybeConverter;
import io.reactivex.rxjava3.core.ObservableConverter;
import io.reactivex.rxjava3.core.SingleConverter;
import io.reactivex.rxjava3.parallel.ParallelFlowableConverter;

/**
 * A custom converter that implements all the RxJava types converters, for use with the {@code as()}
 * operator.
 *
 * @param <T> the type.
 */
public interface AutoDisposeConverter<T>
    extends FlowableConverter<T, FlowableSubscribeProxy<T>>,
        ParallelFlowableConverter<T, ParallelFlowableSubscribeProxy<T>>,
        ObservableConverter<T, ObservableSubscribeProxy<T>>,
        MaybeConverter<T, MaybeSubscribeProxy<T>>,
        SingleConverter<T, SingleSubscribeProxy<T>>,
        CompletableConverter<CompletableSubscribeProxy> {}
