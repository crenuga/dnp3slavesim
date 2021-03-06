/**
 * Copyright 2011 Green Energy Corp.
 *
 * Licensed to Green Energy Corp (www.greenenergycorp.com) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  Green Energy Corp licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.totalgrid.reef.dnp3sim;


import org.totalgrid.reef.protocol.dnp3.Analog;
import org.totalgrid.reef.protocol.dnp3.Binary;
import org.totalgrid.reef.protocol.dnp3.Counter;
import org.totalgrid.reef.protocol.dnp3.IDataObserver;

import java.util.*;

public class ObserverUpdater {
    private static final int ANALOG_RANGE = 100000;

    private final IDataObserver observer;
    private final DataDimension dimension;
    private Random random;

    private Map<Integer, Boolean> lastStatuses = new HashMap<Integer, Boolean>();

    private final int pointCount;

    public ObserverUpdater(IDataObserver observer, DataDimension dimension, Random random) {
        this.observer = observer;
        this.dimension = dimension;
        this.random = random;

        pointCount = dimension.getStatusCount() + dimension.getAnalogCount() + dimension.getCounterCount();
    }


    public int getPointCount() {
        return pointCount;
    }

    public void update(int numPoints) {
        if (numPoints >= pointCount) {
            loadAll();
        } else {
            observer.Start();

            Set<Integer> selected = new HashSet<Integer>();
            for (int i = 0; i < numPoints; i++) {
                int gi = SetSelect.selectIndex(random.nextInt(pointCount), selected, pointCount);
                selected.add(gi);
                updatePoint(gi);
            }

            observer.End();
        }
    }

    protected void updatePoint(int globalIndex) {
        if (globalIndex < dimension.getStatusCount()) {
            updateStatus(globalIndex);
        } else if (globalIndex < (dimension.getStatusCount() + dimension.getAnalogCount())) {
            updateAnalog(globalIndex - dimension.getStatusCount());
        } else {
            updateCounter(globalIndex - dimension.getAnalogCount() - dimension.getAnalogCount());
        }
    }

    protected void updateStatus(int index) {
        observer.Update(randomBinary(index), index);
    }
    protected void updateAnalog(int index) {
        observer.Update(randomAnalog(), index);
    }
    protected void updateCounter(int index) {
        observer.Update(randomCounter(), index);
    }

    public void loadAll() {
        observer.Start();
        for (int i = 0; i < dimension.getStatusCount(); i++) {
            observer.Update(randomBinary(i), i);
        }
        for (int i = 0; i < dimension.getAnalogCount(); i++) {
            observer.Update(randomAnalog(), i);
        }
        for (int i = 0; i < dimension.getCounterCount(); i++) {
            observer.Update(randomCounter(), i);
        }
        observer.End();
    }


    protected Binary randomBinary(int index) {
        Binary b = new Binary();
        b.SetToNow();

        boolean value = true;
        if (lastStatuses.containsKey(index)) {
            value = !lastStatuses.get(index);
        }
        lastStatuses.put(index, value);

        b.SetValue(value);
        b.SetQuality((short)0);
        return b;
    }

    protected Analog randomAnalog() {
        Analog a = new Analog();
        a.SetToNow();
        a.SetQuality((short)0);
        a.SetValue(random.nextDouble()*ANALOG_RANGE);
        return a;
    }

    protected Counter randomCounter() {
        Counter c = new Counter();
        c.SetToNow();
        c.SetQuality((short)0);
        c.SetValue(random.nextInt());
        return c;
    }
}
