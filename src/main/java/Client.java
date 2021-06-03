import java.net.Socket;
import java.io.*;
import java.util.function.Consumer;

interface MyListener
{
    void dataReceived(GameInfo g);
}

public class Client extends Thread
{
    Socket socketClient;
    private boolean kill = false;
    ObjectOutputStream out;
    ObjectInputStream in;
    String ip;
    int port;
    //Observer pattern so our main GUI can be notified of GameInfo objects being sent to the client, but not an arraylist since we only have the GUI observing
    MyListener listener;

    private Consumer<Serializable> callback;

    Client(Consumer<Serializable> call, String i, int p)
    {
        callback = call;
        ip = i;
        port = p;
        setDaemon(true);
    }

    public void kill()
    {
        kill = true;
        try
        {
            socketClient.close();
        }catch(Exception ex)
        {

        }
    }

    public void setListener(MyListener l)
    {
        listener = l;
    }

    public void run()
    {
        try
        {
            socketClient= new Socket(ip, port);
            out = new ObjectOutputStream(socketClient.getOutputStream());
            in = new ObjectInputStream(socketClient.getInputStream());
            socketClient.setTcpNoDelay(true);
        }
        catch(Exception e)
        {
            callback.accept("Failed to join server.");
            kill = true;
        }

        while(!kill)
        {
            try
            {
                //Try to read data, and print to listView if it contains a message
                GameInfo g = (GameInfo)in.readObject();
                if (!g.msg.equals("")) callback.accept(g.msg);
                //Notify the GUI of the message we received so it can update
                listener.dataReceived(g);
            }
            //Lost connection to the server
            catch(Exception e)
            {
                callback.accept("Server connection dropped.  Exit game.");
                //Notify the GUI to disable the buttons
                GameInfo g = new GameInfo(5);
                listener.dataReceived(g);
                kill = true;
            }
        }
    }

    //Send data to the server
    public void send(GameInfo data)
    {
        try
        {
            out.writeObject(data);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
