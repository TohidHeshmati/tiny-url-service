package org.tohid.tinyurlservice.config

import org.springframework.core.convert.converter.Converter
import org.tohid.tinyurlservice.controller.dtos.Granularity
import java.util.Locale

class StringToGranularityConverter : Converter<String, Granularity> {
    override fun convert(source: String): Granularity = Granularity.valueOf(source.uppercase(Locale.getDefault()))
}
