/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yqman.widget.app;

import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

/**
 * Created by yqman on 2016/5/10.
 * 这里只是一个简单的封装
 * 动画部分相对复杂很多，在此建议使用：第三方开源库https://github.com/wasabeef/recyclerview-animators
 * implements 'jp.wasabeef:recyclerview-animators:2.2.0'
 * recyclerView.setItemAnimator(new SlideInUpAnimator(new OvershootInterpolator(0.1f)));
 * recyclerView.getItemAnimator().setAddDuration(100);
 * recyclerView.getItemAnimator().setRemoveDuration(100);
 * recyclerView.getItemAnimator().setMoveDuration(100);
 * recyclerView.getItemAnimator().setChangeDuration(100);
 */
public class ItemDisplayAnimation extends SlideInUpAnimator {
    public ItemDisplayAnimation() {
        this(new OvershootInterpolator(0.1f), 100);
    }

    public ItemDisplayAnimation(Interpolator interpolator, int durationTime) {
        super(interpolator);
        setAddDuration(durationTime);
        setRemoveDuration(durationTime);
        setMoveDuration(durationTime);
        setChangeDuration(durationTime);
    }
}