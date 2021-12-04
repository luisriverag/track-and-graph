/*
 *  This file is part of Track & Graph
 *
 *  Track & Graph is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Track & Graph is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Track & Graph.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.samco.trackandgraph.functionslib

import com.samco.trackandgraph.database.dto.IDataPoint
import com.samco.trackandgraph.database.entity.DataType
import com.samco.trackandgraph.functionslib.exceptions.InvalidRegularityException
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.temporal.TemporalAmount

/**
 * Requires regular input. Makes sure that the output sample at least contains data up to
 * the end time and down to the start time. No clipping is performed here, so if the input sample is
 * larger than the given time range then the input sample will not be modified.
 */
class DataPaddingFunction : DataSampleFunction {
    private val timeHelper: TimeHelper
    private val endTime: OffsetDateTime
    private val startTime: OffsetDateTime
    private val defaultValue: Double
    private val defaultDataType: DataType
    private val defaultLabel: String

    constructor(
        timeHelper: TimeHelper,
        endTime: OffsetDateTime,
        duration: TemporalAmount,
        defaultValue: Double = 0.0,
        defaultLabel: String = "",
        defaultDataType: DataType = DataType.CONTINUOUS
    ) {
        this.timeHelper = timeHelper
        this.endTime = endTime
        this.startTime = this.endTime.minus(duration)
        this.defaultValue = defaultValue
        this.defaultLabel = defaultLabel
        this.defaultDataType = defaultDataType
    }

    constructor(
        timeHelper: TimeHelper,
        endTime: OffsetDateTime,
        startTime: OffsetDateTime,
        defaultValue: Double = 0.0,
        defaultLabel: String = "",
        defaultDataType: DataType = DataType.CONTINUOUS
    ) {
        this.timeHelper = timeHelper
        this.endTime = endTime
        this.startTime = startTime
        this.defaultValue = defaultValue
        this.defaultLabel = defaultLabel
        this.defaultDataType = defaultDataType
    }

    override suspend fun mapSample(dataSample: DataSample): DataSample {
        if (dataSample.dataSampleProperties.regularity == null) throw InvalidRegularityException()
        return DataSample.fromSequence(
            getSequence(dataSample, dataSample.dataSampleProperties.regularity),
            dataSample.dataSampleProperties
        )
    }

    private fun getSequence(dataSample: DataSample, period: TemporalAmount) = sequence {
        val first = dataSample.firstOrNull()?.let { timeHelper.toZonedDateTime(it.timestamp) }
        if (first == null) {
            yieldAll(fullRange(period))
            return@sequence
        }
        var current = timeHelper.findEndOfTemporal(endTime, period).minus(period)

        while (current > first) {
            yield(createDataPoint(current.toOffsetDateTime()))
            current = current.minus(period)
        }
        val iterator = dataSample.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            yield(next)
            if (!iterator.hasNext()) {
                current = timeHelper.toZonedDateTime(next.timestamp)
                    .minus(period)
            }
        }
        val beginning = timeHelper.toZonedDateTime(startTime)
        while (current > beginning) {
            yield(createDataPoint(current.toOffsetDateTime()))
            current = current.minus(period)
        }
    }

    private fun fullRange(period: TemporalAmount) = sequence {
        val end = timeHelper.findEndOfTemporal(startTime, period)
        var current = timeHelper.findEndOfTemporal(endTime, period).minus(period)
        while (current >= end) {
            yield(createDataPoint(current.toOffsetDateTime()))
            current = current.minus(period)
        }
    }

    private fun createDataPoint(timestamp: OffsetDateTime): IDataPoint = object : IDataPoint() {
        override val timestamp = timestamp
        override val dataType: DataType = defaultDataType
        override val value: Double = defaultValue
        override val label: String = defaultLabel
        override val note: String = ""
    }
}