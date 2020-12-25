package com.iconloop.iitpvault.util;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.exception.XMLParserException;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;

public class GenMybatisTestUtils {

    @Test
    public void generate() throws IOException, XMLParserException, InvalidConfigurationException, SQLException, InterruptedException {
        List<String> warningList = Lists.newArrayList();
        boolean overwrite = true;
        String projectPath = Paths.get(Paths.get(".").toAbsolutePath().toString()).getParent().toAbsolutePath().toString();
        Path filePath = Paths.get(projectPath, "script", "generatorConfig.xml");

//        Path deletePath = Paths.get(projectPath, "src/main/java/com/iconloop/visitme/domain");

//        if(Files.exists(deletePath)) {
//            MoreFiles.deleteRecursively(deletePath, RecursiveDeleteOption.ALLOW_INSECURE);
//        }
        File configFile = filePath.toFile();
        ConfigurationParser cp = new ConfigurationParser(warningList);
        Configuration config = cp.parseConfiguration(configFile);
        DefaultShellCallback callback = new DefaultShellCallback(overwrite);
        MyBatisGenerator generator = new MyBatisGenerator(config, callback, warningList);
        generator.generate(null);
    }
}
