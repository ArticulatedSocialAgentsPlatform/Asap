package asap.opensmile;

import hmi.util.Resources;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.google.common.base.Charsets;
import com.google.common.primitives.Doubles;

/**
 * Calls OpenSmile
 * @author hvanwelbergen
 * @FIXME Very hacked: using opensmile over the user interface, parses the resulting .csv and multiplies energy with 0.2. 
 */
public class OpenSmileWrapper
{
    public static void smilextract(String configfile, String input, String output) throws IOException
    {
        File configFile = File.createTempFile("configfile", ".conf");
        PrintWriter out = new PrintWriter(configFile);
        out.println(Resources.readResource(configfile));
        out.close();

        ProcessBuilder pb = new ProcessBuilder();
        pb.command("SMILExtract", "-C", configFile.toString(), "-I", input, "-O", output);
        Process p = pb.start();
        try
        {
            p.waitFor();
        }
        catch (InterruptedException e)
        {
            Thread.interrupted();
        }        
    }

    @Data
    public static class AudioFeatures
    {
        final double[] f0;
        final double[] rmsEnergy;
        final double duration;
        public double getFrameDuration()
        {
            return duration/(double)f0.length;
        }
    }
    
    public static AudioFeatures analyzeProsody(String input) throws IOException
    {
        File prosodyFile = File.createTempFile("prosody", ".csv");
        smilextract("prosodyAcf.conf", input, prosodyFile.toString());        
        CSVParser parser = CSVParser.parse(prosodyFile, Charsets.UTF_8, CSVFormat.RFC4180.withIgnoreSurroundingSpaces(true)
                .withHeader());
        List<Double> f0Values = new ArrayList<>();
        List<Double> rmsValues = new ArrayList<>();
        double duration = 0;
        for (CSVRecord rec : parser.getRecords())
        {
            f0Values.add(Double.parseDouble(rec.get("F0_sma")));
            rmsValues.add(0.2*Double.parseDouble(rec.get("pcm_RMSenergy_sma")));
            duration = Double.parseDouble(rec.get("frameTime"));
        }
        parser.close();
        return new AudioFeatures(Doubles.toArray(f0Values), Doubles.toArray(rmsValues), duration);
    }
}
