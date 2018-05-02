package com.example.cv0318.instagramclone.Models;

public class UserSettings
{
    private User m_user;
    private UserAccountSettings m_userAccountSettings;

    public UserSettings(
        User user,
        UserAccountSettings userAccountSettings)
    {
        m_user = user;
        m_userAccountSettings = userAccountSettings;
    }

    public UserSettings()
    {
    }

    public User getUser()
    {
        return m_user;
    }

    public void setUser(User user)
    {
        m_user = user;
    }

    public UserAccountSettings getUserAccountSettings()
    {
        return m_userAccountSettings;
    }

    public void setUserAccountSettings(UserAccountSettings userAccountSettings)
    {
        m_userAccountSettings = userAccountSettings;
    }

    @Override
    public String toString()
    {
        return "UserSettings{" +
            "m_user=" + m_user +
            ", m_userAccountSettings=" + m_userAccountSettings +
            '}';
    }
}
