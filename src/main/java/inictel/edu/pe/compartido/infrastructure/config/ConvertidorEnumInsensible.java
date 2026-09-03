package inictel.edu.pe.compartido.infrastructure.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.lang.NonNull;

/**
 * Convierte parametros de consulta a enums sin distinguir mayusculas
 * (por ejemplo {@code ?estado=disponible} -> {@code EstadoEquipo.DISPONIBLE}).
 */
public class ConvertidorEnumInsensible implements ConverterFactory<String, Enum<?>> {

    @Override
    @NonNull
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T extends Enum<?>> Converter<String, T> getConverter(@NonNull Class<T> tipoDestino) {
        Class<?> tipoEnum = tipoDestino;
        while (tipoEnum != null && !tipoEnum.isEnum()) {
            tipoEnum = tipoEnum.getSuperclass();
        }
        if (tipoEnum == null) {
            throw new IllegalArgumentException("El tipo " + tipoDestino.getName() + " no es un enum.");
        }
        final Class<? extends Enum> objetivo = (Class<? extends Enum>) tipoEnum;
        return origen -> {
            if (origen == null || origen.isBlank()) {
                return null;
            }
            return (T) Enum.valueOf(objetivo, origen.trim().toUpperCase());
        };
    }
}
