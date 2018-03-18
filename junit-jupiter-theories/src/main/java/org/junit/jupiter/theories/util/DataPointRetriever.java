package org.junit.jupiter.theories.util;

import org.junit.jupiter.theories.annotations.DataPoint;
import org.junit.jupiter.theories.annotations.DataPoints;
import org.junit.jupiter.theories.domain.DataPointDetails;
import org.junit.jupiter.theories.exceptions.DataPointRetrievalException;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.junit.platform.commons.util.ReflectionUtils.isStatic;

/**
 * Helper class that is used to retrieve data points from test classes.
 */
public class DataPointRetriever {
    /**
     * Gets all data point details from the provided context.
     *
     * @param context the evaluation context
     * @return a {@code Stream} of data point details
     */
    public List<DataPointDetails> getAllDataPoints(ExtensionContext context) {
        return Stream.concat(getIndividualDataPointValues(context), getDataPointCollectionValues(context))
                .collect(toList());
    }


    /**
     * Retrieves individual data point values (i.e. those marked with {@line DataPoint}).
     *
     * @param context the context
     * @return the retrieved data points
     */
    private Stream<DataPointDetails> getIndividualDataPointValues(ExtensionContext context) {
        return Stream.concat(
                getValues(context, DataPoint.class, DataPoint::qualifiers, Class::getDeclaredFields,
                        DataPointRetriever::getFieldValue, DataPointRetriever::buildDataPoint),
                getValues(context, DataPoint.class, DataPoint::qualifiers, Class::getDeclaredMethods,
                        DataPointRetriever::getMethodValue, DataPointRetriever::buildDataPoint)
        );
    }


    /**
     * Retrieves collections of data point values (i.e. those marked with {@line DataPoints}).
     *
     * @param context the context
     * @return the retrieved data points
     */
    private Stream<DataPointDetails> getDataPointCollectionValues(ExtensionContext context) {
        return Stream.concat(
                getValues(context, DataPoints.class, DataPoints::qualifiers, Class::getDeclaredFields,
                        DataPointRetriever::getFieldValue, DataPointRetriever::buildDataPointsWithCollectionExpansion),
                getValues(context, DataPoints.class, DataPoints::qualifiers, Class::getDeclaredMethods,
                        DataPointRetriever::getMethodValue, DataPointRetriever::buildDataPointsWithCollectionExpansion)
        );
    }


    /**
     * Gets the value of the provided field, wrapping any exceptions with a {@link DataPointRetrievalException}.
     *
     * @param instance the test class instance
     * @param field the field to retrieve
     * @return the retrieved field value
     */
    private static Object getFieldValue(Object instance, Field field) {
        try {
            return field.get(instance);
        } catch (ReflectiveOperationException error) {
            throw new DataPointRetrievalException("Error retrieving data point from field. Reason: " + error.toString(), error);
        }
    }


    /**
     * Gets the value returned from calling the provided zero-argument method, wrapping any exceptions with a {@link DataPointRetrievalException}.
     *
     * @param instance the test class instance
     * @param method the method to call. Must have no parameters.
     * @return the retrieved field value
     */
    private static Object getMethodValue(Object instance, Method method) {
        if (method.getParameters().length != 0) {
            throw new DataPointRetrievalException("Expected method to have zero parameters, but actually had " + method.getParameters().length);
        }
        try {
            return method.invoke(instance);
        } catch (ReflectiveOperationException error) {
            throw new DataPointRetrievalException("Error retrieving data point from method. Reason: " + error.toString(), error);
        }
    }


    /**
     * A basic data point detail factory that turns data points into data point details on a one-to-one basis.
     *
     * @param valueSourceReference a reference to the {@code Member} that the value came from
     * @param valueToProcessIntoDataPoints the value to convert into data points
     * @param qualifiers the qualifiers (if any) for this data point
     * @return a {@code Stream} containing the constructed data point
     */
    private static Stream<DataPointDetails> buildDataPoint(Member valueSourceReference, Object valueToProcessIntoDataPoints,
            List<String> qualifiers) {
        return Stream.of(new DataPointDetails(valueToProcessIntoDataPoints, qualifiers, getDataPointSourceName(valueSourceReference)));
    }


    /**
     * A data point factory that expands the first level of collections of data points, including {@code Collection} objects and arrays. If an data
     * point is not a collection, then it will be used as-is. If it is a collection of collections, only the first collection will be expanded.
     *
     * @param valueSourceReference a reference to the {@code Member} that the value came from
     * @param valueToProcessIntoDataPoints the value to convert into data points
     * @param qualifiers the qualifiers (if any) for this data point
     * @return a {@code Stream} of data points
     */
    @SuppressWarnings("unchecked")
    private static Stream<DataPointDetails> buildDataPointsWithCollectionExpansion(Member valueSourceReference, Object valueToProcessIntoDataPoints,
            List<String> qualifiers) {

        Stream<Object> valuesStream;
        if (valueToProcessIntoDataPoints instanceof Collection) {
            valuesStream = ((Collection<Object>) valueToProcessIntoDataPoints).stream();
        } else if (valueToProcessIntoDataPoints.getClass().isArray()) {
            valuesStream = Arrays.stream((Object[]) valueToProcessIntoDataPoints);
        } else {
            //NOTE: Support for Streams and Iterators is intentionally left out because the fact that they're stateful causes concerns about
            //whether tests will be deterministic or not
            return Stream.of(valueToProcessIntoDataPoints)
                    .map(v -> new DataPointDetails(v, qualifiers, getDataPointSourceNameAtIndex(valueSourceReference, 0)));
        }

        AtomicInteger valueIndex = new AtomicInteger(0);
        return valuesStream
                .map(v -> new DataPointDetails(v, qualifiers, getDataPointSourceNameAtIndex(valueSourceReference, valueIndex.getAndIncrement())));
    }


    /**
     * Builds the name for a data point source with an index.
     *
     * @param source the location of the data point source
     * @param index the index of the data point source
     * @return the constructed name
     */
    private static String getDataPointSourceNameAtIndex(Member source, int index) {
        return getDataPointSourceName(source) + "[" + index + "]";
    }


    /**
     * Builds the name for a data point source.
     *
     * @param source the location of the data point source
     * @return the constructed name
     */
    private static String getDataPointSourceName(Member source) {
        return source.getName();
    }


    /**
     * Builds one type of data point details.
     *
     * @param context the context
     * @param targetAnnotation the data point annotation to search for
     * @param qualifierExtractor the extractor that will pull qualifiers from the target annotation
     * @param classElementReferenceExtractor the extractor that will retrieve elements (methods/functions) from the class
     * @param retrievalOperation the operation to perform on the extracted elements
     * @param dataPointDetailsFactory the factory that will convert values into data points
     * @param <T> the type of element being parsed
     * @param <U> the target annotation type
     * @return the constructed data points
     */
    private <T extends AccessibleObject & Member, U extends Annotation> Stream<DataPointDetails> getValues(ExtensionContext context, Class<U> targetAnnotation,
            Function<U, String[]> qualifierExtractor, Function<Class<?>, T[]> classElementReferenceExtractor,
            BiFunction<Object, T, Object> retrievalOperation, DataPointDetailsFactory dataPointDetailsFactory) {

        Optional<Object> optionalTestInstance = context.getTestInstance();

        T[] references = classElementReferenceExtractor.apply(context.getRequiredTestClass());
        Stream<T> dataPointReferences = Arrays.stream(references)
                .peek(reference -> reference.setAccessible(true))
                .filter(reference -> reference.getAnnotation(targetAnnotation) != null);

        //Unless the test instance is present we can only retrieve static data points
        if (!optionalTestInstance.isPresent()) {
            dataPointReferences = dataPointReferences
                    .peek(reference -> {
                        if (!isStatic(reference)) {
                            throw new DataPointRetrievalException("Found non-static data point \"" + reference
                                    + "\" but test instance was not available. Only static data points can be used in the current " +
                                    "configuration. (Try setting \"@TestInstance(TestInstance.Lifecycle.PER_CLASS)\" to use non-static " +
                                    "data points)");
                        }
                    });
        }

        return dataPointReferences
                .flatMap(reference -> {
                    try {
                        List<String> qualifiers = Arrays.asList(qualifierExtractor.apply(reference.getAnnotation(targetAnnotation)));
                        Object instanceToGetFieldFrom;
                        if (isStatic(reference)) {
                            instanceToGetFieldFrom = null;
                        } else {
                            instanceToGetFieldFrom = optionalTestInstance.get();
                        }
                        Object referencedValue = retrievalOperation.apply(instanceToGetFieldFrom, reference);
                        return dataPointDetailsFactory.buildDataPoints(reference, referencedValue, qualifiers);
                    } catch (DataPointRetrievalException error) {
                        throw new RuntimeException("Error retrieving data point \"" + reference + "\". " + error.toString(),
                                error);
                    }
                });
    }


    /**
     * Internal interface used to create factories that convert values to data point details.
     */
    @FunctionalInterface
    private interface DataPointDetailsFactory {
        /**
         * Turns data values into data point details.
         *
         * @param sourceReference a reference to the {@code Member} that the value came from
         * @param sourceValue the value to convert into data points
         * @param qualifiers the qualifiers (if any) for this data point
         * @return a {@code Stream} containing the constructed data point
         */
        Stream<DataPointDetails> buildDataPoints(Member sourceReference, Object sourceValue, List<String> qualifiers);
    }
}
