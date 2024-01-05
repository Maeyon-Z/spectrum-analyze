package com.hit.spectrum.data;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.hit.spectrum.config.Params;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SpectrumDb {

    public static List<SpectrumDbData>  loadDbData(){
        List<SpectrumDbData> curves = new ArrayList<>();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            String url = Params.dbUrl;
            String username = Params.user;
            String password = Params.password;

            Connection connection = DriverManager.getConnection(url, username, password);

            Statement statement = connection.createStatement();

            String sql = "SELECT * FROM tb_spectrum_data where is_delete = 0 order by id asc;";
            ResultSet resultSet = statement.executeQuery(sql);
            while(resultSet.next()){
                SpectrumDbData data = new SpectrumDbData();
                data.setName(resultSet.getString("name"));
                data.setNormalized(resultSet.getString("normalized"));
                curves.add(data);
            }

            resultSet.close();
            statement.close();
            connection.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return curves;
    }

    public static boolean insertData(SpectrumDbData res){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            String url = Params.dbUrl;
            String username = Params.user;
            String password = Params.password;

            Connection connection = DriverManager.getConnection(url, username, password);

            String sql = "insert into tb_spectrum_data" +
                    "(id, `name`, origin, smooth_one, background, corrected, smooth_two, fix_peak, normalized, is_delete) " +
                    "values(?,?,?,?,?,?,?,?,?,?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, null);
            statement.setString(2, res.getName());
            statement.setString(3, res.getOrigin());
            statement.setString(4, res.getSmoothOne());
            statement.setString(5, res.getBackground());
            statement.setString(6, res.getCorrected());
            statement.setString(7, res.getSmoothTwo());
            statement.setString(8, res.getFixPeak());
            statement.setString(9, res.getNormalized());
            statement.setInt(10, 0);
            int result = statement.executeUpdate();
            statement.close();
            connection.close();
            return result == 1;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public static List<SpectrumData> getAll() {
        List<SpectrumData> datas = new ArrayList<>();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            String url = Params.dbUrl;
            String username = Params.user;
            String password = Params.password;

            Connection connection = DriverManager.getConnection(url, username, password);

            Statement statement = connection.createStatement();

            String sql = "SELECT id, `name` FROM tb_spectrum_data where is_delete = 0 order by id asc;";
            ResultSet resultSet = statement.executeQuery(sql);
            while(resultSet.next()){
                SpectrumData data = new SpectrumData();
                data.setId(resultSet.getLong("id"));
                data.setName(resultSet.getString("name"));
                datas.add(data);
            }

            resultSet.close();
            statement.close();
            connection.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return datas;
    }

    public static SpectrumData getStandardById(Long idx) {
        SpectrumData data = new SpectrumData();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            String url = Params.dbUrl;
            String username = Params.user;
            String password = Params.password;

            Connection connection = DriverManager.getConnection(url, username, password);

            String sql = "SELECT * FROM tb_spectrum_data where id = ?";

            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, idx);

            ResultSet resultSet = statement.executeQuery(sql);

            while(resultSet.next()){
                data.setId(resultSet.getLong("id"));
                data.setName(resultSet.getString("name"));
                data.setOrigin(JSON.parseArray(resultSet.getString("origin"), Double.class));
                data.setSmoothOne(JSON.parseArray(resultSet.getString("smooth_one"), Double.class));
                data.setBackground(JSON.parseArray(resultSet.getString("background"), Double.class));
                data.setCorrected(JSON.parseArray(resultSet.getString("corrected"), Double.class));
                data.setSmoothTwo(JSON.parseArray(resultSet.getString("smooth_two"), Double.class));
                data.setFixPeak(JSON.parseArray(resultSet.getString("fix_peak"), Double.class));
                data.setNormalized(JSON.parseArray(resultSet.getString("normalized"), Double.class));
            }

            resultSet.close();
            statement.close();
            connection.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return data;
    }

    public static boolean deleteById(Long id) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            String url = Params.dbUrl;
            String username = Params.user;
            String password = Params.password;

            Connection connection = DriverManager.getConnection(url, username, password);

            String sql = "update tb_spectrum_data set is_delete = 1 where id = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, 1L);
            int result = statement.executeUpdate();
            statement.close();
            connection.close();
            return result == 1;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
}
