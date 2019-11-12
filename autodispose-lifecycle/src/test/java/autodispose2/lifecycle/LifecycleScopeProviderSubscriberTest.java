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
package autodispose2.lifecycle;

import static autodispose2.AutoDispose.autoDisposable;
import static autodispose2.lifecycle.TestUtil.makeLifecycleProvider;
import static com.google.common.truth.Truth.assertThat;

import autodispose2.AutoDisposePlugins;
import autodispose2.OutsideScopeException;
import autodispose2.test.RxErrorsRule;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class LifecycleScopeProviderSubscriberTest {

  @Rule public RxErrorsRule rule = new RxErrorsRule();

  @Before
  @After
  public void resetPlugins() {
    AutoDisposePlugins.reset();
  }

  @Test
  public void autoDispose_withLifecycleProvider() {
    PublishProcessor<Integer> source = PublishProcessor.create();
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.createDefault(0);
    LifecycleScopeProvider<Integer> provider = makeLifecycleProvider(lifecycle);
    TestSubscriber<Integer> o = source.to(autoDisposable(provider)).test();
    assertThat(o.hasSubscription()).isTrue();

    assertThat(source.hasSubscribers()).isTrue();
    assertThat(lifecycle.hasObservers()).isTrue();

    source.onNext(1);
    o.assertValue(1);

    lifecycle.onNext(1);
    source.onNext(2);

    assertThat(source.hasSubscribers()).isTrue();
    assertThat(lifecycle.hasObservers()).isTrue();
    o.assertValues(1, 2);

    lifecycle.onNext(3);
    source.onNext(3);

    // Nothing new
    o.assertValues(1, 2);

    // Unsubscribed
    assertThat(source.hasSubscribers()).isFalse();
    assertThat(lifecycle.hasObservers()).isFalse();
  }

  @Test
  public void autoDispose_withProvider_withoutStartingLifecycle_shouldFail() {
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.create();
    LifecycleScopeProvider<Integer> provider = makeLifecycleProvider(lifecycle);
    TestSubscriber<Integer> o = Flowable.just(1).to(autoDisposable(provider)).test();

    o.assertError(LifecycleNotStartedException.class);
  }

  @Test
  public void autoDispose_withProvider_afterLifecycle_shouldFail() {
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.createDefault(0);
    lifecycle.onNext(1);
    lifecycle.onNext(2);
    lifecycle.onNext(3);
    LifecycleScopeProvider<Integer> provider = makeLifecycleProvider(lifecycle);
    TestSubscriber<Integer> o = Flowable.just(1).to(autoDisposable(provider)).test();

    o.assertError(LifecycleEndedException.class);
  }

  @Test
  public void autoDispose_withProviderAndNoOpPlugin_withoutStarting_shouldFailSilently() {
    AutoDisposePlugins.setOutsideScopeHandler(e -> {});
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.create();
    LifecycleScopeProvider<Integer> provider = makeLifecycleProvider(lifecycle);
    PublishProcessor<Integer> source = PublishProcessor.create();
    TestSubscriber<Integer> o = source.to(autoDisposable(provider)).test();

    assertThat(source.hasSubscribers()).isFalse();
    assertThat(lifecycle.hasObservers()).isFalse();
    o.assertNoValues();
    o.assertNoErrors();
  }

  @Test
  public void autoDispose_withProviderAndNoOpPlugin_afterEnding_shouldFailSilently() {
    AutoDisposePlugins.setOutsideScopeHandler(
        e -> {
          // Noop
        });
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.createDefault(0);
    lifecycle.onNext(1);
    lifecycle.onNext(2);
    lifecycle.onNext(3);
    LifecycleScopeProvider<Integer> provider = makeLifecycleProvider(lifecycle);
    PublishProcessor<Integer> source = PublishProcessor.create();
    TestSubscriber<Integer> o = source.to(autoDisposable(provider)).test();

    assertThat(source.hasSubscribers()).isFalse();
    assertThat(lifecycle.hasObservers()).isFalse();
    o.assertNoValues();
    o.assertNoErrors();
  }

  @Test
  public void autoDispose_withProviderAndPlugin_withoutStarting_shouldFailWithExp() {
    AutoDisposePlugins.setOutsideScopeHandler(
        e -> {
          // Wrap in an IllegalStateException so we can verify this is the exception we see on the
          // other side
          throw new IllegalStateException(e);
        });
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.create();
    LifecycleScopeProvider<Integer> provider = makeLifecycleProvider(lifecycle);
    PublishProcessor<Integer> source = PublishProcessor.create();
    TestSubscriber<Integer> o = source.to(autoDisposable(provider)).test();

    o.assertNoValues();
    o.assertError(
        throwable ->
            throwable instanceof IllegalStateException
                && throwable.getCause() instanceof OutsideScopeException);
  }
}
